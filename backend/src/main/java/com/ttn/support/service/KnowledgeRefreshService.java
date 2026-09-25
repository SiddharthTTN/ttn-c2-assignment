package com.ttn.support.service;

import com.ttn.support.domain.KnowledgeSourceType;
import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.domain.Ticket;
import com.ttn.support.domain.TicketComment;
import com.ttn.support.domain.TicketKnowledge;
import com.ttn.support.rag.ChunkingService;
import com.ttn.support.rag.ContentHasher;
import com.ttn.support.rag.EmbeddingCodec;
import com.ttn.support.rag.KnowledgeChunkFormatter;
import com.ttn.support.rag.TicketEmbeddingService;
import com.ttn.support.repository.TicketCommentRepository;
import com.ttn.support.repository.TicketKnowledgeRepository;
import com.ttn.support.repository.TicketRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class KnowledgeRefreshService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRefreshService.class);

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketKnowledgeRepository knowledgeRepository;
    private final ChunkingService chunkingService;
    private final ContentHasher contentHasher;
    private final TicketEmbeddingService embeddingService;
    private final EmbeddingCodec embeddingCodec;
    private final Environment environment;
    private final KnowledgeFailureRecorder knowledgeFailureRecorder;
    private final KnowledgeChunkFormatter knowledgeChunkFormatter;
    private final TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public KnowledgeRefreshService(
            TicketRepository ticketRepository,
            TicketCommentRepository commentRepository,
            TicketKnowledgeRepository knowledgeRepository,
            ChunkingService chunkingService,
            ContentHasher contentHasher,
            TicketEmbeddingService embeddingService,
            EmbeddingCodec embeddingCodec,
            Environment environment,
            KnowledgeFailureRecorder knowledgeFailureRecorder,
            KnowledgeChunkFormatter knowledgeChunkFormatter,
            PlatformTransactionManager transactionManager) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.chunkingService = chunkingService;
        this.contentHasher = contentHasher;
        this.embeddingService = embeddingService;
        this.embeddingCodec = embeddingCodec;
        this.environment = environment;
        this.knowledgeFailureRecorder = knowledgeFailureRecorder;
        this.knowledgeChunkFormatter = knowledgeChunkFormatter;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void refreshTicket(String ticketId) {
        refreshTicket(ticketId, true);
    }

    private void refreshTicket(String ticketId, boolean refreshOneNewerVersion) {
        RefreshPlan plan = transactionTemplate.execute(status -> loadRefreshPlan(ticketId));
        if (plan == null) {
            return;
        }

        List<float[]> embeddings;
        try {
            embeddings = embeddingService.embedAll(plan.texts());
        } catch (RuntimeException ex) {
            knowledgeFailureRecorder.recordFailure(ticketId, plan.knowledgeVersion());
            log.warn("Knowledge refresh failed ticketId={} reason={}", ticketId, ex.getMessage());
            throw ex;
        }

        try {
            Boolean applied = transactionTemplate.execute(status -> applyRefreshPlan(plan, embeddings));
            if (!Boolean.TRUE.equals(applied)) {
                log.debug(
                        "Skipped stale knowledge refresh ticketId={} expectedVersion={}",
                        ticketId,
                        plan.knowledgeVersion());
                if (refreshOneNewerVersion) {
                    refreshNewerPendingVersion(ticketId, plan.knowledgeVersion());
                }
                return;
            }
            log.info(
                    "Knowledge refresh succeeded ticketId={} version={}",
                    ticketId,
                    plan.knowledgeVersion());
        } catch (RuntimeException ex) {
            scheduleFailureRecording(ticketId, plan.knowledgeVersion());
            log.warn("Knowledge refresh failed ticketId={} reason={}", ticketId, ex.getMessage());
            throw ex;
        }
    }

    private RefreshPlan loadRefreshPlan(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null) {
            return null;
        }
        if (ticket.getKnowledgeState() != KnowledgeState.PENDING) {
            log.debug(
                    "Skipping knowledge refresh ticketId={} state={}",
                    ticketId,
                    ticket.getKnowledgeState());
            return null;
        }
        List<ChunkDraft> drafts = buildChunks(ticket);
        List<String> texts = drafts.stream().map(ChunkDraft::content).toList();
        return new RefreshPlan(ticket, drafts, texts);
    }

    private boolean applyRefreshPlan(RefreshPlan plan, List<float[]> embeddings) {
        Ticket ticket =
                ticketRepository.findByIdForKnowledgeRefresh(plan.ticket().getId()).orElse(null);
        if (ticket == null) {
            return false;
        }
        if (ticket.getKnowledgeState() != KnowledgeState.PENDING) {
            return false;
        }
        if (ticket.getKnowledgeVersion() != plan.knowledgeVersion()) {
            return false;
        }

        String ticketId = ticket.getId();
        knowledgeRepository.deleteByTicketId(ticketId);
        knowledgeRepository.flush();

        Instant now = Instant.now();
        for (int i = 0; i < plan.drafts().size(); i++) {
            ChunkDraft draft = plan.drafts().get(i);
            TicketKnowledge knowledge = TicketKnowledge.createNew();
            knowledge.setTicketId(ticket.getId());
            knowledge.setSourceType(draft.sourceType().name());
            knowledge.setSourceId(draft.sourceId());
            knowledge.setChunkIndex(draft.chunkIndex());
            knowledge.setContent(draft.content());
            knowledge.setContentHash(contentHasher.sha256(draft.content()));
            knowledge.setTicketVersion(ticket.getKnowledgeVersion());
            knowledge.setStatus(ticket.getStatus().name());
            knowledge.setPriority(ticket.getPriority().name());
            knowledge.setAssignee(ticket.getAssignee());
            knowledge.setCategory(ticket.getCategory());
            knowledge.setCreatedAt(now);
            String payload = embeddingCodec.encode(embeddings.get(i));
            if (isPostgresProfile()) {
                insertPgVectorRow(knowledge, payload, embeddings.get(i));
            } else {
                knowledge.setEmbeddingPayload(payload);
                knowledgeRepository.save(knowledge);
            }
        }

        ticket.setKnowledgeState(KnowledgeState.READY);
        ticket.setKnowledgeRetryCount(0);
        ticket.setKnowledgeNextRetryAt(null);
        ticket.setUpdatedAt(Instant.now());
        ticketRepository.save(ticket);
        return true;
    }

    private void insertPgVectorRow(TicketKnowledge knowledge, String embeddingPayload, float[] embedding) {
        String vector = EmbeddingCodec.toPgVectorLiteral(EmbeddingCodec.normalize(embedding));
        entityManager
                .createNativeQuery(
                        """
                        INSERT INTO ticket_knowledge (
                          id, ticket_id, source_type, source_id, chunk_index, content, content_hash,
                          ticket_version, status, priority, assignee, category, embedding_payload, embedding, created_at
                        ) VALUES (
                          :id, :ticketId, :sourceType, :sourceId, :chunkIndex, :content, :contentHash,
                          :ticketVersion, :status, :priority, :assignee, :category, :embeddingPayload,
                          CAST(:embedding AS vector), :createdAt
                        )
                        """)
                .setParameter("id", knowledge.getId())
                .setParameter("ticketId", knowledge.getTicketId())
                .setParameter("sourceType", knowledge.getSourceType())
                .setParameter("sourceId", knowledge.getSourceId())
                .setParameter("chunkIndex", knowledge.getChunkIndex())
                .setParameter("content", knowledge.getContent())
                .setParameter("contentHash", knowledge.getContentHash())
                .setParameter("ticketVersion", knowledge.getTicketVersion())
                .setParameter("status", knowledge.getStatus())
                .setParameter("priority", knowledge.getPriority())
                .setParameter("assignee", knowledge.getAssignee())
                .setParameter("category", knowledge.getCategory())
                .setParameter("embeddingPayload", embeddingPayload)
                .setParameter("embedding", vector)
                .setParameter("createdAt", knowledge.getCreatedAt())
                .executeUpdate();
    }

    private List<ChunkDraft> buildChunks(Ticket ticket) {
        List<ChunkDraft> drafts = new ArrayList<>();
        addFieldChunks(drafts, ticket, KnowledgeSourceType.DESCRIPTION, "description", ticket.getDescription());
        if (ticket.getResolutionNotes() != null && !ticket.getResolutionNotes().isBlank()) {
            addFieldChunks(
                    drafts,
                    ticket,
                    KnowledgeSourceType.RESOLUTION_NOTES,
                    "resolution",
                    ticket.getResolutionNotes());
        }
        for (TicketComment comment : commentRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId())) {
            addFieldChunks(
                    drafts,
                    ticket,
                    KnowledgeSourceType.COMMENT,
                    String.valueOf(comment.getId()),
                    comment.getBody());
        }
        return drafts;
    }

    private void addFieldChunks(
            List<ChunkDraft> drafts,
            Ticket ticket,
            KnowledgeSourceType sourceType,
            String sourceId,
            String text) {
        List<String> chunks = chunkingService.chunkField(text);
        for (int i = 0; i < chunks.size(); i++) {
            String content = chunks.get(i);
            drafts.add(new ChunkDraft(
                    ticket.getId(),
                    sourceType,
                    sourceId,
                    i,
                    knowledgeChunkFormatter.formatChunk(ticket, sourceType, content)));
        }
    }

    private void refreshNewerPendingVersion(String ticketId, long staleVersion) {
        RefreshPlan newerPlan = transactionTemplate.execute(status -> loadRefreshPlan(ticketId));
        if (newerPlan != null && newerPlan.knowledgeVersion() != staleVersion) {
            refreshTicket(ticketId, false);
        }
    }

    private void scheduleFailureRecording(String ticketId, long expectedKnowledgeVersion) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            knowledgeFailureRecorder.recordFailure(ticketId, expectedKnowledgeVersion);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    knowledgeFailureRecorder.recordFailure(ticketId, expectedKnowledgeVersion);
                }
            }
        });
    }

    private boolean isPostgresProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("postgres".equals(profile)) {
                return true;
            }
        }
        return false;
    }

    private record RefreshPlan(Ticket ticket, List<ChunkDraft> drafts, List<String> texts) {
        long knowledgeVersion() {
            return ticket.getKnowledgeVersion();
        }
    }

    private record ChunkDraft(
            String ticketId, KnowledgeSourceType sourceType, String sourceId, int chunkIndex, String content) {}
}
