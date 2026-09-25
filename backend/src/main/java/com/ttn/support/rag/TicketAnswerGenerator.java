package com.ttn.support.rag;

import java.util.List;
import java.util.Set;

public interface TicketAnswerGenerator {

    GeneratedAnswer generate(String question, List<RetrievedChunk> chunks, Set<String> allowedTicketIds);

    record GeneratedAnswer(String answer, List<String> ticketIds) {}
}
