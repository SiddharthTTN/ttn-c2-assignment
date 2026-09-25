package com.ttn.support.web.dto;

import com.ttn.support.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

public class StatusChangeRequest {

    @NotNull
    private TicketStatus status;

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}
