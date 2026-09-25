package com.ttn.support.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TicketPriority {
    LOW,
    MEDIUM,
    HIGH;

    @JsonCreator
    public static TicketPriority from(String value) {
        if (value == null) {
            return null;
        }
        return TicketPriority.valueOf(value.trim().toUpperCase());
    }
}
