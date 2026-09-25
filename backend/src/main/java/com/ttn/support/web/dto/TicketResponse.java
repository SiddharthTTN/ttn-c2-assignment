package com.ttn.support.web.dto;

import com.ttn.support.domain.Ticket;
import com.ttn.support.domain.TicketComment;
import com.ttn.support.domain.TicketPriority;
import com.ttn.support.domain.TicketStatus;
import java.time.Instant;
import java.util.List;

public class TicketResponse {

    private String id;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private String assignee;
    private String category;
    private String resolutionNotes;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CommentResponse> comments;

    public static TicketResponse from(Ticket ticket, List<TicketComment> comments) {
        TicketResponse response = new TicketResponse();
        response.id = ticket.getId();
        response.title = ticket.getTitle();
        response.description = ticket.getDescription();
        response.status = ticket.getStatus();
        response.priority = ticket.getPriority();
        response.assignee = ticket.getAssignee();
        response.category = ticket.getCategory();
        response.resolutionNotes = ticket.getResolutionNotes();
        response.createdAt = ticket.getCreatedAt();
        response.updatedAt = ticket.getUpdatedAt();
        response.comments = comments.stream().map(CommentResponse::from).toList();
        return response;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getCategory() {
        return category;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<CommentResponse> getComments() {
        return comments;
    }
}
