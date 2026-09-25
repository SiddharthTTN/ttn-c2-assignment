package com.ttn.support.web.dto;

import com.ttn.support.domain.TicketComment;
import java.time.Instant;

public record CommentResponse(Long id, String body, Instant createdAt) {

    public static CommentResponse from(TicketComment comment) {
        return new CommentResponse(comment.getId(), comment.getBody(), comment.getCreatedAt());
    }
}
