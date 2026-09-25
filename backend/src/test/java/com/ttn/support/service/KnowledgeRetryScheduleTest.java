package com.ttn.support.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.domain.Ticket;
import com.ttn.support.repository.TicketRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(properties = "spring.profiles.active=test")
@TestPropertySource(properties = "app.knowledge-retry.max-attempts=5")
class KnowledgeRetryScheduleTest {

    @Autowired
    private KnowledgeFailureRecorder failureRecorder;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private KnowledgeRetryBackoff backoff;

    @BeforeEach
    void reset() {
        Ticket ticket = ticketRepository.findById("TKT-1001").orElseThrow();
        ticket.setKnowledgeState(KnowledgeState.PENDING);
        ticket.setKnowledgeRetryCount(0);
        ticket.setKnowledgeNextRetryAt(null);
        ticketRepository.save(ticket);
    }

    @Test
    void schedulesNextRetryUsingBackoff() {
        failureRecorder.recordFailure("TKT-1001");
        Ticket ticket = ticketRepository.findById("TKT-1001").orElseThrow();
        assertEquals(1, ticket.getKnowledgeRetryCount());
        assertEquals(KnowledgeState.PENDING, ticket.getKnowledgeState());
        assertTrue(ticket.getKnowledgeNextRetryAt().isAfter(Instant.now()));

        Instant expected = Instant.now().plusMillis(backoff.delayMsForAttempt(1));
        assertTrue(ticket.getKnowledgeNextRetryAt().getEpochSecond() >= expected.getEpochSecond() - 2);
    }

    @Test
    void dueQueryExcludesFutureRetries() {
        failureRecorder.recordFailure("TKT-1001");
        Ticket ticket = ticketRepository.findById("TKT-1001").orElseThrow();
        assertTrue(ticketRepository
                .findPendingKnowledgeRefreshDue(KnowledgeState.PENDING, Instant.now())
                .isEmpty());
        assertEquals(
                1,
                ticketRepository
                        .findPendingKnowledgeRefreshDue(
                                KnowledgeState.PENDING, ticket.getKnowledgeNextRetryAt().plusSeconds(1))
                        .size());
    }
}
