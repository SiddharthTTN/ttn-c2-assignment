package com.ttn.support.config;

import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.domain.Ticket;
import com.ttn.support.repository.TicketRepository;
import com.ttn.support.service.KnowledgeRefreshService;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test & !pgvector-it")
public class StartupKnowledgeInitializer implements ApplicationRunner {

    private final TicketRepository ticketRepository;
    private final KnowledgeRefreshService knowledgeRefreshService;

    public StartupKnowledgeInitializer(
            TicketRepository ticketRepository, KnowledgeRefreshService knowledgeRefreshService) {
        this.ticketRepository = ticketRepository;
        this.knowledgeRefreshService = knowledgeRefreshService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Ticket> pending = ticketRepository.findAll().stream()
                .filter(t -> t.getKnowledgeState() == KnowledgeState.PENDING)
                .toList();
        for (Ticket ticket : pending) {
            try {
                knowledgeRefreshService.refreshTicket(ticket.getId());
            } catch (Exception ignored) {
                // Scheduler will retry.
            }
        }
    }
}
