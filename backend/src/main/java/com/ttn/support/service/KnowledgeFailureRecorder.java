package com.ttn.support.service;

import com.ttn.support.config.KnowledgeRetryProperties;
import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.domain.Ticket;
import com.ttn.support.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeFailureRecorder {

    private final TicketRepository ticketRepository;
    private final KnowledgeRetryProperties knowledgeRetryProperties;
    private final KnowledgeRetryBackoff knowledgeRetryBackoff;

    public KnowledgeFailureRecorder(
            TicketRepository ticketRepository,
            KnowledgeRetryProperties knowledgeRetryProperties,
            KnowledgeRetryBackoff knowledgeRetryBackoff) {
        this.ticketRepository = ticketRepository;
        this.knowledgeRetryProperties = knowledgeRetryProperties;
        this.knowledgeRetryBackoff = knowledgeRetryBackoff;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null) {
            return;
        }
        long nextAttempt = ticket.getKnowledgeRetryCount() + 1;
        ticket.setKnowledgeRetryCount(nextAttempt);
        if (nextAttempt >= knowledgeRetryProperties.getMaxAttempts()) {
            ticket.setKnowledgeState(KnowledgeState.FAILED);
            ticket.setKnowledgeNextRetryAt(null);
        } else {
            ticket.setKnowledgeState(KnowledgeState.PENDING);
            ticket.setKnowledgeNextRetryAt(knowledgeRetryBackoff.nextRetryAt(nextAttempt));
        }
        ticketRepository.save(ticket);
    }
}
