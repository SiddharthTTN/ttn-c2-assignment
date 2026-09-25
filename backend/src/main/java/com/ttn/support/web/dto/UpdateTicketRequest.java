package com.ttn.support.web.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ttn.support.domain.TicketPriority;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@JsonDeserialize(using = UpdateTicketRequestDeserializer.class)
public class UpdateTicketRequest {

    private boolean titlePresent;
    private boolean descriptionPresent;
    private boolean priorityPresent;
    private boolean assigneePresent;
    private boolean categoryPresent;
    private boolean resolutionNotesPresent;

    private String title;
    private String description;
    private TicketPriority priority;
    private String assignee;
    private String category;
    private String resolutionNotes;

    public boolean hasAnyField() {
        return titlePresent
                || descriptionPresent
                || priorityPresent
                || assigneePresent
                || categoryPresent
                || resolutionNotesPresent;
    }

    public boolean isTitlePresent() {
        return titlePresent;
    }

    public void setTitlePresent(boolean titlePresent) {
        this.titlePresent = titlePresent;
    }

    public boolean isDescriptionPresent() {
        return descriptionPresent;
    }

    public void setDescriptionPresent(boolean descriptionPresent) {
        this.descriptionPresent = descriptionPresent;
    }

    public boolean isPriorityPresent() {
        return priorityPresent;
    }

    public void setPriorityPresent(boolean priorityPresent) {
        this.priorityPresent = priorityPresent;
    }

    public boolean isAssigneePresent() {
        return assigneePresent;
    }

    public void setAssigneePresent(boolean assigneePresent) {
        this.assigneePresent = assigneePresent;
    }

    public boolean isCategoryPresent() {
        return categoryPresent;
    }

    public void setCategoryPresent(boolean categoryPresent) {
        this.categoryPresent = categoryPresent;
    }

    public boolean isResolutionNotesPresent() {
        return resolutionNotesPresent;
    }

    public void setResolutionNotesPresent(boolean resolutionNotesPresent) {
        this.resolutionNotesPresent = resolutionNotesPresent;
    }

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
