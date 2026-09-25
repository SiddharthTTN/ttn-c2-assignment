package com.ttn.support.web.dto;

import com.ttn.support.domain.TicketPriority;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateTicketRequest", description = "Partial ticket update; omitted fields are unchanged.")
public class UpdateTicketPatchSchema {

    @Schema(maxLength = 200)
    private String title;

    @Schema(maxLength = 20000)
    private String description;

    private TicketPriority priority;

    @Schema(maxLength = 120, nullable = true)
    private String assignee;

    @Schema(maxLength = 80, nullable = true)
    private String category;

    @Schema(maxLength = 20000, nullable = true)
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
