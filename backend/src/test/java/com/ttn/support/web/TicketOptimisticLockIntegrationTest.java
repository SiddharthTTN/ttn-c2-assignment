package com.ttn.support.web;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ttn.support.domain.Ticket;
import com.ttn.support.domain.TicketPriority;
import com.ttn.support.domain.TicketStatus;
import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.repository.TicketCommentRepository;
import com.ttn.support.repository.TicketRepository;
import com.ttn.support.service.TicketService;
import com.ttn.support.web.dto.UpdateTicketRequest;
import com.ttn.support.web.error.ConflictException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@SpringBootTest(properties = "spring.profiles.active=test")
class TicketOptimisticLockIntegrationTest {

    @Autowired
    private TicketService ticketService;

    @MockBean
    private TicketRepository ticketRepository;

    @MockBean
    private TicketCommentRepository commentRepository;

    @Test
    void patchMapsOptimisticLockConflictTo409() {
        Ticket ticket = sampleTicket("TKT-9001");
        when(ticketRepository.findById("TKT-9001")).thenReturn(Optional.of(ticket));
        when(ticketRepository.saveAndFlush(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Ticket.class, "TKT-9001"));
        when(commentRepository.findByTicketIdOrderByCreatedAtAscIdAsc("TKT-9001")).thenReturn(List.of());

        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setTitlePresent(true);
        request.setTitle("updated");

        assertThrows(ConflictException.class, () -> ticketService.update("TKT-9001", request));
    }

    @Test
    void commentMapsOptimisticLockConflictTo409() {
        Ticket ticket = sampleTicket("TKT-9002");
        when(ticketRepository.findById("TKT-9002")).thenReturn(Optional.of(ticket));
        when(ticketRepository.saveAndFlush(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Ticket.class, "TKT-9002"));

        assertThrows(ConflictException.class, () -> ticketService.addComment("TKT-9002", "body"));
    }

    private static Ticket sampleTicket(String id) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTitle("title");
        ticket.setDescription("desc");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.LOW);
        ticket.setKnowledgeState(KnowledgeState.READY);
        ticket.setKnowledgeVersion(1);
        ticket.setKnowledgeRetryCount(0);
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
        return ticket;
    }
}
