package com.ttn.support.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.domain.Ticket;
import com.ttn.support.rag.DeterministicTicketEmbeddingService;
import com.ttn.support.rag.TicketEmbeddingService;
import com.ttn.support.repository.TicketKnowledgeRepository;
import com.ttn.support.repository.TicketRepository;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = "spring.profiles.active=test")
@Import(KnowledgeRefreshTransactionBoundaryTest.ProbeConfig.class)
class KnowledgeRefreshTransactionBoundaryTest {

    @Autowired
    private KnowledgeRefreshService knowledgeRefreshService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketKnowledgeRepository knowledgeRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ProbeConfig probeConfig;

    @BeforeEach
    void resetTicket() {
        probeConfig.simulateStaleVersionDuringEmbedding = false;
        transactionTemplate.executeWithoutResult(status -> {
            Ticket ticket = ticketRepository.findById("TKT-1001").orElseThrow();
            ticket.setKnowledgeState(KnowledgeState.PENDING);
            ticket.setKnowledgeRetryCount(0);
            ticket.setKnowledgeVersion(1);
            ticketRepository.save(ticket);
            knowledgeRepository.deleteByTicketId("TKT-1001");
            knowledgeRepository.flush();
        });
    }

    @Test
    void embeddingRunsOutsideActiveTransaction() {
        probeConfig.simulateStaleVersionDuringEmbedding = false;
        knowledgeRefreshService.refreshTicket("TKT-1001");

        assertFalse(probeConfig.embeddingObservedInsideTransaction.get());
        assertEquals(KnowledgeState.READY, ticketRepository.findById("TKT-1001").orElseThrow().getKnowledgeState());
    }

    @Test
    void staleKnowledgeVersionDoesNotReplaceChunksOrRecordFailure() {
        knowledgeRefreshService.refreshTicket("TKT-1001");
        int chunksAfterReady = knowledgeRepository.findByTicketId("TKT-1001").size();
        assertTrue(chunksAfterReady > 0);

        transactionTemplate.executeWithoutResult(status -> {
            Ticket ticket = ticketRepository.findById("TKT-1001").orElseThrow();
            ticket.setKnowledgeState(KnowledgeState.PENDING);
            ticket.setKnowledgeVersion(1);
            ticketRepository.save(ticket);
        });

        probeConfig.simulateStaleVersionDuringEmbedding = true;
        knowledgeRefreshService.refreshTicket("TKT-1001");

        Ticket ticket = ticketRepository.findById("TKT-1001").orElseThrow();
        assertEquals(KnowledgeState.PENDING, ticket.getKnowledgeState());
        assertTrue(ticket.getKnowledgeVersion() > 1);
        assertEquals(chunksAfterReady, knowledgeRepository.findByTicketId("TKT-1001").size());
        assertEquals(0, ticket.getKnowledgeRetryCount());
    }

    @TestConfiguration
    static class ProbeConfig {

        final AtomicBoolean embeddingObservedInsideTransaction = new AtomicBoolean(false);
        boolean simulateStaleVersionDuringEmbedding;

        @Bean
        @Primary
        TicketEmbeddingService probingEmbeddingService(
                DeterministicTicketEmbeddingService delegate,
                TicketRepository ticketRepository,
                TransactionTemplate transactionTemplate) {
            return new TicketEmbeddingService() {
                @Override
                public float[] embed(String text) {
                    return delegate.embed(text);
                }

                @Override
                public List<float[]> embedAll(List<String> texts) {
                    embeddingObservedInsideTransaction.set(
                            TransactionSynchronizationManager.isActualTransactionActive());
                    if (simulateStaleVersionDuringEmbedding) {
                        transactionTemplate.executeWithoutResult(status -> {
                            Ticket ticket = ticketRepository.findById("TKT-1001").orElseThrow();
                            ticket.setKnowledgeVersion(ticket.getKnowledgeVersion() + 1);
                            ticket.setKnowledgeState(KnowledgeState.PENDING);
                            ticketRepository.save(ticket);
                        });
                    }
                    return delegate.embedAll(texts);
                }
            };
        }
    }
}
