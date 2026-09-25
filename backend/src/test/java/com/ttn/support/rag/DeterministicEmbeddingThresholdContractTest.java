package com.ttn.support.rag;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DeterministicEmbeddingThresholdContractTest {

    @Test
    void deterministicEmbeddingsDoNotReachProductionDefaultThresholdForBroadQueries() {
        float[] question = DeterministicTicketEmbeddingService.embedDeterministic(
                "What caused previous payment failures?", 768);
        float[] chunk = DeterministicTicketEmbeddingService.embedDeterministic(
                "Ticket TKT-1001 status RESOLVED priority HIGH category payments "
                        + "Customer reports card payment declined with error PAY-402",
                768);
        double similarity = EmbeddingCodec.cosineSimilarity(
                EmbeddingCodec.normalize(question), EmbeddingCodec.normalize(chunk));
        assertTrue(
                similarity < 0.75,
                "documents why H2/test retrieval keeps a lexical prefilter while still applying the 0.75 cutoff");
    }
}
