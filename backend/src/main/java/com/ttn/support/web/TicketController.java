package com.ttn.support.web;

import com.ttn.support.domain.TicketStatus;
import com.ttn.support.service.TicketService;
import com.ttn.support.web.dto.CommentRequest;
import com.ttn.support.web.dto.CreateTicketRequest;
import com.ttn.support.web.dto.StatusChangeRequest;
import com.ttn.support.web.dto.TicketListResponse;
import com.ttn.support.web.dto.TicketResponse;
import com.ttn.support.web.dto.UpdateTicketRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request));
    }

    @GetMapping
    public TicketListResponse list(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "status", required = false) String status) {
        TicketStatus parsedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                parsedStatus = TicketStatus.from(status);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid status value: " + status);
            }
        }
        return new TicketListResponse(ticketService.list(query, parsedStatus));
    }

    @GetMapping("/{id}")
    public TicketResponse get(@PathVariable String id) {
        return ticketService.get(id);
    }

    @PatchMapping("/{id}")
    public TicketResponse update(@PathVariable String id, @Valid @RequestBody UpdateTicketRequest request) {
        return ticketService.update(id, request);
    }

    @PostMapping("/{id}/status")
    public TicketResponse changeStatus(@PathVariable String id, @Valid @RequestBody StatusChangeRequest request) {
        return ticketService.changeStatus(id, request.getStatus());
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<TicketResponse> addComment(
            @PathVariable String id, @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.addComment(id, request.getBody()));
    }
}
