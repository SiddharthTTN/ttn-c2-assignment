package com.ttn.support.web.dto;

import java.util.List;

public class AskResponse {

    private String answer;
    private List<String> ticketIds;
    private boolean noRelevantTickets;

    public AskResponse() {}

    public AskResponse(String answer, List<String> ticketIds, boolean noRelevantTickets) {
        this.answer = answer;
        this.ticketIds = ticketIds;
        this.noRelevantTickets = noRelevantTickets;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getTicketIds() {
        return ticketIds;
    }

    public void setTicketIds(List<String> ticketIds) {
        this.ticketIds = ticketIds;
    }

    public boolean isNoRelevantTickets() {
        return noRelevantTickets;
    }

    public void setNoRelevantTickets(boolean noRelevantTickets) {
        this.noRelevantTickets = noRelevantTickets;
    }
}
