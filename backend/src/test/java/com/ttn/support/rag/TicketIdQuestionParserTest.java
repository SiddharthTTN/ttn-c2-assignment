package com.ttn.support.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class TicketIdQuestionParserTest {

    @Test
    void extractsNormalizedTicketIds() {
        assertEquals(
                List.of("TKT-1001"),
                TicketIdQuestionParser.extractTicketIds("What was the resolution for ticket TKT-1001?"));
    }
}
