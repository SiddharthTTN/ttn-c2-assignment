package com.ttn.support.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED;

    @JsonCreator
    public static TicketStatus from(String value) {
        if (value == null) {
            return null;
        }
        return TicketStatus.valueOf(value.trim().toUpperCase());
    }
}
