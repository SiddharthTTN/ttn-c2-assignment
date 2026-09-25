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

    public KnowledgeFailureRecorder(
            TicketRepository ticketRepository, KnowledgeRetryProperties knowledgeRetryProperties) {
        this.ticketRepository = ticketRepository;
        this.knowledgeRetryProperties = knowledgeRetryProperties;
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
        } else {
            ticket.setKnowledgeState(KnowledgeState.PENDING);
        }
        ticketRepository.save(ticket);
    }
}
