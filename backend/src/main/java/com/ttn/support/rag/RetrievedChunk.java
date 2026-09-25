package com.ttn.support.rag;

public record RetrievedChunk(
        String ticketId,
        String content,
        double similarity,
        long ticketVersion) {}
