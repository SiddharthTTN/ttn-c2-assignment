package com.ttn.support.service;

import com.ttn.support.domain.TicketStatus;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StatusTransitionService {

    private static final Logger log = LoggerFactory.getLogger("com.ttn.support.transition");

    private final Map<TicketStatus, Set<TicketStatus>> allowed = new EnumMap<>(TicketStatus.class);

    public StatusTransitionService() {
        allowed.put(TicketStatus.OPEN, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
        allowed.put(TicketStatus.IN_PROGRESS, EnumSet.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED));
        allowed.put(TicketStatus.RESOLVED, EnumSet.of(TicketStatus.CLOSED));
        allowed.put(TicketStatus.CLOSED, EnumSet.noneOf(TicketStatus.class));
        allowed.put(TicketStatus.CANCELLED, EnumSet.noneOf(TicketStatus.class));
    }

    public boolean isAllowed(TicketStatus from, TicketStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return allowed.getOrDefault(from, EnumSet.noneOf(TicketStatus.class)).contains(to);
    }

    public void logAttempt(String ticketId, TicketStatus previous, TicketStatus requested, boolean accepted) {
        log.info(
                "ticketId={} previousStatus={} requestedStatus={} outcome={}",
                ticketId,
                previous,
                requested,
                accepted ? "ACCEPTED" : "REJECTED");
    }
}
