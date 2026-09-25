package com.ttn.support.web;

import com.ttn.support.web.dto.UpdateTicketRequest;
import com.ttn.support.web.error.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class UpdateTicketRequestValidator {

    private static final int TITLE_MAX = 200;
    private static final int DESCRIPTION_MAX = 20000;
    private static final int ASSIGNEE_MAX = 120;
    private static final int CATEGORY_MAX = 80;

    public void validate(UpdateTicketRequest request) {
        if (!request.hasAnyField()) {
            throw new BadRequestException("At least one field must be provided");
        }
        if (request.isTitlePresent()) {
            validateNonBlankWithMax(request.getTitle(), "title", TITLE_MAX);
        }
        if (request.isDescriptionPresent()) {
            validateNonBlankWithMax(request.getDescription(), "description", DESCRIPTION_MAX);
        }
        if (request.isAssigneePresent() && request.getAssignee() != null) {
            validateMaxLength(request.getAssignee(), "assignee", ASSIGNEE_MAX);
        }
        if (request.isCategoryPresent() && request.getCategory() != null) {
            validateMaxLength(request.getCategory(), "category", CATEGORY_MAX);
        }
        if (request.isResolutionNotesPresent() && request.getResolutionNotes() != null) {
            validateMaxLength(request.getResolutionNotes(), "resolutionNotes", DESCRIPTION_MAX);
        }
    }

    private void validateNonBlankWithMax(String value, String field, int max) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(field + " must not be blank");
        }
        validateMaxLength(value, field, max);
    }

    private void validateMaxLength(String value, String field, int max) {
        if (value.length() > max) {
            throw new BadRequestException(field + " must be at most " + max + " characters");
        }
    }
}
