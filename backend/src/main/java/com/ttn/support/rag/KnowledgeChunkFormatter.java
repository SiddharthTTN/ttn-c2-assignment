package com.ttn.support.rag;

import com.ttn.support.domain.KnowledgeSourceType;
import com.ttn.support.domain.Ticket;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeChunkFormatter {

    public String formatChunk(Ticket ticket, KnowledgeSourceType sourceType, String sourceText) {
        StringBuilder builder = new StringBuilder(256 + sourceText.length());
        builder.append("ticketId=").append(ticket.getId());
        builder.append(" title=").append(ticket.getTitle());
        builder.append(" status=").append(ticket.getStatus().name());
        builder.append(" priority=").append(ticket.getPriority().name());
        if (ticket.getAssignee() != null && !ticket.getAssignee().isBlank()) {
            builder.append(" assignee=").append(ticket.getAssignee());
        }
        if (ticket.getCategory() != null && !ticket.getCategory().isBlank()) {
            builder.append(" category=").append(ticket.getCategory());
        }
        builder.append(" source=").append(sourceType.name());
        builder.append('\n');
        builder.append(sourceText);
        return builder.toString();
    }
}
