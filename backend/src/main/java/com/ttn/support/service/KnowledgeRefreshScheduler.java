package com.ttn.support.service;

import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.domain.Ticket;
import com.ttn.support.repository.TicketRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class KnowledgeRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRefreshScheduler.class);

    private final TicketRepository ticketRepository;
    private final KnowledgeRefreshService knowledgeRefreshService;

    public KnowledgeRefreshScheduler(
            TicketRepository ticketRepository, KnowledgeRefreshService knowledgeRefreshService) {
        this.ticketRepository = ticketRepository;
        this.knowledgeRefreshService = knowledgeRefreshService;
    }

    @Scheduled(fixedDelayString = "${app.knowledge-retry.delay-ms}")
    public void retryPending() {
        List<Ticket> pending = ticketRepository.findAll().stream()
                .filter(t -> t.getKnowledgeState() == KnowledgeState.PENDING)
                .toList();
        for (Ticket ticket : pending) {
            try {
                knowledgeRefreshService.refreshTicket(ticket.getId());
            } catch (Exception ex) {
                log.debug("Scheduled knowledge retry still failing ticketId={}", ticket.getId());
            }
        }
    }
}
