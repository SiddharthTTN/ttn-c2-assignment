package com.ttn.support.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.domain.Ticket;
import com.ttn.support.repository.TicketKnowledgeRepository;
import com.ttn.support.repository.TicketRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = "spring.profiles.active=test")
class KnowledgeRefreshConcurrencyTest {

    @Autowired
    private KnowledgeRefreshService knowledgeRefreshService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketKnowledgeRepository knowledgeRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void resetTicketToPending() {
        transactionTemplate.executeWithoutResult(status -> {
            Ticket ticket = ticketRepository.findById("TKT-1001").orElseThrow();
            ticket.setKnowledgeState(KnowledgeState.PENDING);
            ticket.setKnowledgeRetryCount(0);
            ticketRepository.save(ticket);
            knowledgeRepository.deleteByTicketId("TKT-1001");
            knowledgeRepository.flush();
        });
    }

    @Test
    void concurrentRefreshAttemptsDoNotCreateDuplicateChunks() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(6);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            futures.add(pool.submit(() -> {
                try {
                    knowledgeRefreshService.refreshTicket("TKT-1001");
                } catch (RuntimeException ignored) {
                    // Another worker may have already completed the refresh.
                }
            }));
        }
        for (Future<?> future : futures) {
            future.get();
        }
        pool.shutdown();

        Ticket refreshed = ticketRepository.findById("TKT-1001").orElseThrow();
        assertEquals(KnowledgeState.READY, refreshed.getKnowledgeState());

        int chunkCount = knowledgeRepository.findByTicketId("TKT-1001").size();
        assertTrue(chunkCount > 0);

        knowledgeRefreshService.refreshTicket("TKT-1001");
        assertEquals(chunkCount, knowledgeRepository.findByTicketId("TKT-1001").size());
    }
}
