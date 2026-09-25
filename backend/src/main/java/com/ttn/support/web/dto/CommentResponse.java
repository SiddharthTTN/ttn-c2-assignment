package com.ttn.support.web.dto;

import com.ttn.support.domain.TicketComment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "CommentResponse")
public record CommentResponse(
        @Schema(description = "Comment id") Long id,
        @Schema(description = "Owning ticket id") String ticketId,
        @Schema(description = "Comment body") String body,
        @Schema(description = "Creation timestamp") Instant createdAt) {

    public static CommentResponse from(TicketComment comment) {
        return new CommentResponse(
                comment.getId(), comment.getTicketId(), comment.getBody(), comment.getCreatedAt());
    }
}
