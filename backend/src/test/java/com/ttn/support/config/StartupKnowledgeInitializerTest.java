package com.ttn.support.config;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.domain.Ticket;
import com.ttn.support.repository.TicketRepository;
import com.ttn.support.service.KnowledgeRefreshService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class StartupKnowledgeInitializerTest {

    @Test
    void refreshesOnlyTicketsWhoseRetryIsDue() {
        TicketRepository repository = org.mockito.Mockito.mock(TicketRepository.class);
        KnowledgeRefreshService refreshService = org.mockito.Mockito.mock(KnowledgeRefreshService.class);
        Ticket due = new Ticket();
        due.setId("TKT-DUE");
        when(repository.findPendingKnowledgeRefreshDue(
                        org.mockito.ArgumentMatchers.eq(KnowledgeState.PENDING),
                        org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(List.of(due));

        new StartupKnowledgeInitializer(repository, refreshService)
                .run(new DefaultApplicationArguments(new String[0]));

        verify(refreshService).refreshTicket("TKT-DUE");
        verify(repository, never()).findAll();
    }
}
