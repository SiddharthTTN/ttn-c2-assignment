package com.ttn.support.service;

import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.domain.Ticket;
import com.ttn.support.domain.TicketComment;
import com.ttn.support.domain.TicketPriority;
import com.ttn.support.domain.TicketStatus;
import com.ttn.support.repository.TicketCommentRepository;
import com.ttn.support.repository.TicketRepository;
import com.ttn.support.repository.TicketSearchRepository;
import com.ttn.support.web.UpdateTicketRequestValidator;
import com.ttn.support.web.dto.CommentResponse;
import com.ttn.support.web.dto.CreateTicketRequest;
import com.ttn.support.web.dto.TicketResponse;
import com.ttn.support.web.dto.UpdateTicketRequest;
import com.ttn.support.web.error.ConflictException;
import com.ttn.support.web.error.NotFoundException;
import jakarta.persistence.OptimisticLockException;
import java.time.Instant;
import java.util.List;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketSearchRepository ticketSearchRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketIdGenerator ticketIdGenerator;
    private final SearchPatternBuilder searchPatternBuilder;
    private final StatusTransitionService statusTransitionService;
    private final KnowledgeRefreshPublisher knowledgeRefreshPublisher;
    private final UpdateTicketRequestValidator updateTicketRequestValidator;

    public TicketService(
            TicketRepository ticketRepository,
            TicketSearchRepository ticketSearchRepository,
            TicketCommentRepository commentRepository,
            TicketIdGenerator ticketIdGenerator,
            SearchPatternBuilder searchPatternBuilder,
            StatusTransitionService statusTransitionService,
            KnowledgeRefreshPublisher knowledgeRefreshPublisher,
            UpdateTicketRequestValidator updateTicketRequestValidator) {
        this.ticketRepository = ticketRepository;
        this.ticketSearchRepository = ticketSearchRepository;
        this.commentRepository = commentRepository;
        this.ticketIdGenerator = ticketIdGenerator;
        this.searchPatternBuilder = searchPatternBuilder;
        this.statusTransitionService = statusTransitionService;
        this.knowledgeRefreshPublisher = knowledgeRefreshPublisher;
        this.updateTicketRequestValidator = updateTicketRequestValidator;
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest request) {
        Instant now = Instant.now();
        Ticket ticket = new Ticket();
        ticket.setId(ticketIdGenerator.nextId());
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setAssignee(request.getAssignee());
        ticket.setCategory(request.getCategory());
        ticket.setResolutionNotes(request.getResolutionNotes());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setKnowledgeState(KnowledgeState.PENDING);
        ticket.setKnowledgeVersion(1);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);
        knowledgeRefreshPublisher.scheduleAfterCommit(ticket.getId());
        return toResponse(ticket, List.of());
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> list(String query, TicketStatus status) {
        String pattern = searchPatternBuilder.toLikePattern(query);
        return ticketSearchRepository.search(status, pattern).stream()
                .map(t -> toResponse(t, List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse get(String id) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
        List<TicketComment> comments = commentRepository.findByTicketIdOrderByCreatedAtAscIdAsc(id);
        return toResponse(ticket, comments);
    }

    @Transactional
    public TicketResponse update(String id, UpdateTicketRequest request) {
        updateTicketRequestValidator.validate(request);
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
        boolean changed = false;
        if (request.isTitlePresent()) {
            ticket.setTitle(request.getTitle());
            changed = true;
        }
        if (request.isDescriptionPresent()) {
            ticket.setDescription(request.getDescription());
            changed = true;
        }
        if (request.isPriorityPresent()) {
            ticket.setPriority(request.getPriority());
            changed = true;
        }
        if (request.isAssigneePresent()) {
            ticket.setAssignee(request.getAssignee());
            changed = true;
        }
        if (request.isCategoryPresent()) {
            ticket.setCategory(request.getCategory());
            changed = true;
        }
        if (request.isResolutionNotesPresent()) {
            ticket.setResolutionNotes(request.getResolutionNotes());
            changed = true;
        }
        if (changed) {
            ticket.setKnowledgeVersion(ticket.getKnowledgeVersion() + 1);
            ticket.setKnowledgeState(KnowledgeState.PENDING);
            ticket.setKnowledgeRetryCount(0);
            ticket.setUpdatedAt(Instant.now());
            ticketRepository.save(ticket);
            knowledgeRefreshPublisher.scheduleAfterCommit(ticket.getId());
        }
        List<TicketComment> comments = commentRepository.findByTicketIdOrderByCreatedAtAscIdAsc(id);
        return toResponse(ticket, comments);
    }

    @Transactional
    public TicketResponse changeStatus(String id, TicketStatus requested) {
        Ticket ticket;
        try {
            ticket = ticketRepository.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
            TicketStatus previous = ticket.getStatus();
            boolean allowed = statusTransitionService.isAllowed(previous, requested);
            statusTransitionService.logAttempt(id, previous, requested, allowed);
            if (!allowed) {
                throw new ConflictException("Invalid status transition");
            }
            if (previous != requested) {
                ticket.setStatus(requested);
                ticket.setKnowledgeVersion(ticket.getKnowledgeVersion() + 1);
                ticket.setKnowledgeState(KnowledgeState.PENDING);
                ticket.setKnowledgeRetryCount(0);
                ticket.setUpdatedAt(Instant.now());
                ticketRepository.save(ticket);
                knowledgeRefreshPublisher.scheduleAfterCommit(ticket.getId());
            }
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException ex) {
            throw new ConflictException("Ticket was updated concurrently");
        }
        List<TicketComment> comments = commentRepository.findByTicketIdOrderByCreatedAtAscIdAsc(id);
        return toResponse(ticket, comments);
    }

    @Transactional
    public CommentResponse addComment(String id, String body) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
        TicketComment comment = new TicketComment();
        comment.setTicketId(id);
        comment.setBody(body);
        comment.setCreatedAt(Instant.now());
        commentRepository.save(comment);
        ticket.setKnowledgeVersion(ticket.getKnowledgeVersion() + 1);
        ticket.setKnowledgeState(KnowledgeState.PENDING);
        ticket.setKnowledgeRetryCount(0);
        ticket.setUpdatedAt(Instant.now());
        ticketRepository.save(ticket);
        knowledgeRefreshPublisher.scheduleAfterCommit(id);
        return CommentResponse.from(comment);
    }

    private TicketResponse toResponse(Ticket ticket, List<TicketComment> comments) {
        return TicketResponse.from(ticket, comments);
    }
}
