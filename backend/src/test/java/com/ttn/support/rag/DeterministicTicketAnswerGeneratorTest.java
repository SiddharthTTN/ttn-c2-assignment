package com.ttn.support.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DeterministicTicketAnswerGeneratorTest {

    private final DeterministicTicketAnswerGenerator generator =
            new DeterministicTicketAnswerGenerator(new ObjectMapper());

    @Test
    void prefersAllowedExplicitTicketAndUsesOnlyPrimaryContext() {
        TicketAnswerGenerator.GeneratedAnswer answer = generator.generate(
                "What happened to TKT-1001 and TKT-9999?",
                List.of(
                        new RetrievedChunk("TKT-1001", "payment recovered", 0.9, 1),
                        new RetrievedChunk("TKT-1002", "shipment delayed", 0.8, 1)),
                Set.of("TKT-1001", "TKT-1002"));

        assertEquals(List.of("TKT-1001"), answer.ticketIds());
        assertTrue(answer.answer().contains("payment recovered"));
    }

    @Test
    void ranksByOverlapAndReturnsNoMatchWithoutOverlap() {
        List<RetrievedChunk> chunks = List.of(
                new RetrievedChunk("TKT-1001", "payment checkout failure", 0.9, 1),
                new RetrievedChunk("TKT-1002", "shipment delayed", 0.8, 1));

        assertEquals(
                List.of("TKT-1001"),
                generator.generate("checkout payment", chunks, Set.of("TKT-1001", "TKT-1002"))
                        .ticketIds());
        assertEquals(
                "no relevant tickets found",
                generator.generate("unrelated", chunks, Set.of("TKT-1001", "TKT-1002"))
                        .answer());
    }

    @Test
    void boundsLongContextSummary() {
        String longContext = "x".repeat(300);
        TicketAnswerGenerator.GeneratedAnswer answer = generator.generate(
                "TKT-1001",
                List.of(new RetrievedChunk("TKT-1001", longContext, 1.0, 1)),
                Set.of("TKT-1001"));
        assertTrue(answer.answer().endsWith("..."));
    }

    @Test
    void parsesStructuredAndFallbackCitationsAgainstAllowedSet() {
        Set<String> allowed = Set.of("TKT-1001", "TKT-1002");
        assertEquals(
                List.of("TKT-1001"),
                generator.parseCitations(
                        "{\"ticketIds\":[\"TKT-1001\",\"TKT-1001\",\"TKT-9999\"]}", allowed));
        assertEquals(
                List.of("TKT-1002"),
                generator.parseCitations("fallback prose cites TKT-1002", allowed));
        assertTrue(generator.parseCitations("{\"answer\":\"none\"}", allowed).isEmpty());
    }
}
