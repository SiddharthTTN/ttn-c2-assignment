package com.ttn.support.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.domain.Ticket;
import com.ttn.support.domain.TicketPriority;
import com.ttn.support.domain.TicketStatus;
import com.ttn.support.repository.TicketRepository;
import com.ttn.support.web.dto.UpdateTicketRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.profiles.active=test")
@Transactional
class TicketOperationPerformanceTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketService ticketService;

    @Test
    void ticketOperationsStayWithinP95TargetAtOneThousandTickets() {
        seedToOneThousandTickets();

        assertP95BelowTarget("list", () -> ticketService.list(null, null));
        assertP95BelowTarget("details", () -> ticketService.get("TKT-PERF-TARGET"));

        UpdateTicketRequest update = new UpdateTicketRequest();
        update.setAssigneePresent(true);
        update.setAssignee("Performance Agent");
        assertP95BelowTarget("update", () -> ticketService.update("TKT-PERF-TARGET", update));
    }

    private void seedToOneThousandTickets() {
        Ticket target = newTicket("TKT-PERF-TARGET", 0);
        ticketRepository.saveAndFlush(target);
        int existing = Math.toIntExact(ticketRepository.count());
        List<Ticket> tickets = new ArrayList<>();
        for (int i = existing; i < 1000; i++) {
            tickets.add(newTicket("TKT-PERF-" + String.format("%04d", i), i));
        }
        ticketRepository.saveAllAndFlush(tickets);
    }

    private Ticket newTicket(String id, int number) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTitle("Performance ticket " + number);
        ticket.setDescription("Bounded benchmark record");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setKnowledgeState(KnowledgeState.READY);
        ticket.setKnowledgeVersion(1);
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
        return ticket;
    }

    private void assertP95BelowTarget(String operation, Runnable invocation) {
        invocation.run();
        List<Long> samples = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            long start = System.nanoTime();
            invocation.run();
            samples.add(Duration.ofNanos(System.nanoTime() - start).toMillis());
        }
        Collections.sort(samples);
        long p95 = samples.get((int) Math.ceil(samples.size() * 0.95) - 1);
        assertTrue(p95 < 500, operation + " p95 was " + p95 + "ms");
    }
}
