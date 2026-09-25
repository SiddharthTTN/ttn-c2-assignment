package com.ttn.support.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ttn.support.domain.TicketStatus;
import org.junit.jupiter.api.Test;

class StatusTransitionServiceTest {

    private final StatusTransitionService service = new StatusTransitionService();

    @Test
    void allowsDocumentedTransitions() {
        assertTrue(service.isAllowed(TicketStatus.OPEN, TicketStatus.IN_PROGRESS));
        assertTrue(service.isAllowed(TicketStatus.OPEN, TicketStatus.CANCELLED));
        assertTrue(service.isAllowed(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED));
        assertTrue(service.isAllowed(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
        assertTrue(service.isAllowed(TicketStatus.RESOLVED, TicketStatus.CLOSED));
    }

    @Test
    void rejectsForbiddenTransitions() {
        assertFalse(service.isAllowed(TicketStatus.OPEN, TicketStatus.RESOLVED));
        assertFalse(service.isAllowed(TicketStatus.OPEN, TicketStatus.OPEN));
        assertFalse(service.isAllowed(TicketStatus.CLOSED, TicketStatus.OPEN));
        assertFalse(service.isAllowed(TicketStatus.CANCELLED, TicketStatus.OPEN));
        assertFalse(service.isAllowed(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS));
    }
}
