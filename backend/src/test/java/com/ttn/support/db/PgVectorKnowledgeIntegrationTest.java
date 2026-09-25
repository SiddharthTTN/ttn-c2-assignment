package com.ttn.support.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.domain.Ticket;
import com.ttn.support.rag.KnowledgeRetrievalService;
import com.ttn.support.rag.TicketEmbeddingService;
import com.ttn.support.rag.RagProperties;
import com.ttn.support.rag.RetrievedChunk;
import com.ttn.support.repository.TicketKnowledgeRepository;
import com.ttn.support.repository.TicketRepository;
import com.ttn.support.service.KnowledgeRefreshService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Live PostgreSQL/pgvector coverage (Flyway, native vector insert, retrieval filters).
 * Skipped automatically when Docker is unavailable.
 */
@EnabledIfDockerAvailable
@Testcontainers
@SpringBootTest
@ActiveProfiles({"postgres", "pgvector-it"})
class PgVectorKnowledgeIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("pgvector/pgvector:pg16")
                    .withDatabaseName("support_tickets")
                    .withUsername("support")
                    .withPassword("change-me-local-db-password");

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private KnowledgeRefreshService knowledgeRefreshService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketKnowledgeRepository knowledgeRepository;

    @Autowired
    private KnowledgeRetrievalService knowledgeRetrievalService;

    @Autowired
    private RagProperties ragProperties;

    @Autowired
    private TicketEmbeddingService embeddingService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void markPending() {
        transactionTemplate.executeWithoutResult(status -> {
            Ticket ticket = ticketRepository.findById("TKT-1001").orElseThrow();
            ticket.setKnowledgeState(KnowledgeState.PENDING);
            ticket.setKnowledgeVersion(1);
            ticket.setKnowledgeRetryCount(0);
            ticketRepository.save(ticket);
            knowledgeRepository.deleteByTicketId("TKT-1001");
            knowledgeRepository.flush();
        });
    }

    @Test
    void flywaySchemaSupportsVectorInsertRetrievalAndReplacement() {
        knowledgeRefreshService.refreshTicket("TKT-1001");

        Ticket ready = ticketRepository.findById("TKT-1001").orElseThrow();
        assertEquals(KnowledgeState.READY, ready.getKnowledgeState());
        assertFalse(knowledgeRepository.findByTicketId("TKT-1001").isEmpty());

        float[] query = embeddingService.embed("payment checkout failure");
        List<RetrievedChunk> hits = knowledgeRetrievalService.retrieve(
                query, "payment checkout failure", ragProperties.getTopK(), ragProperties.getSimilarityThreshold());
        assertFalse(hits.isEmpty());
        assertEquals(
                1,
                knowledgeRetrievalService.retrieve(query, "payment checkout failure", 1, 0.0).size(),
                "topK must bound the result set");
        assertTrue(
                knowledgeRetrievalService.retrieve(query, "payment checkout failure", 8, 1.0).isEmpty(),
                "a strict cutoff must exclude non-identical vectors");

        ready.setKnowledgeState(KnowledgeState.PENDING);
        ready.setKnowledgeVersion(2);
        ticketRepository.save(ready);
        knowledgeRefreshService.refreshTicket("TKT-1001");

        Ticket replaced = ticketRepository.findById("TKT-1001").orElseThrow();
        assertEquals(2, replaced.getKnowledgeVersion());
        assertEquals(KnowledgeState.READY, replaced.getKnowledgeState());
        assertTrue(knowledgeRepository.findByTicketId("TKT-1001").stream()
                .allMatch(chunk -> chunk.getTicketVersion() == 2));
    }

    @Test
    void staleKnowledgeVersionChunksAreExcludedFromRetrieval() {
        knowledgeRefreshService.refreshTicket("TKT-1001");

        Ticket ticket = ticketRepository.findById("TKT-1001").orElseThrow();
        ticket.setKnowledgeVersion(ticket.getKnowledgeVersion() + 1);
        ticket.setKnowledgeState(KnowledgeState.PENDING);
        ticketRepository.save(ticket);

        float[] query = embeddingService.embed("payment checkout failure");
        List<RetrievedChunk> hits = knowledgeRetrievalService.retrieve(
                query, "payment checkout failure", ragProperties.getTopK(), ragProperties.getSimilarityThreshold());
        assertTrue(hits.isEmpty(), "stale chunks must not match current ticket version");
    }
}
