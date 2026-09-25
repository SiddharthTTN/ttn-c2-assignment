package com.ttn.support.web.dto;

import com.ttn.support.domain.TicketPriority;
import jakarta.validation.constraints.Size;

public class UpdateTicketRequest {

    @Size(max = 200)
    private String title;

    private String description;

    private TicketPriority priority;

    @Size(max = 120)
    private String assignee;

    @Size(max = 80)
    private String category;

    private String resolutionNotes;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
