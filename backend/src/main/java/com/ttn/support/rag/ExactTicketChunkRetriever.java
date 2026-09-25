package com.ttn.support.rag;

import com.ttn.support.domain.TicketKnowledge;
import com.ttn.support.repository.TicketKnowledgeRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ExactTicketChunkRetriever {

    private final TicketKnowledgeRepository knowledgeRepository;
    private final EmbeddingCodec embeddingCodec;

    public ExactTicketChunkRetriever(
            TicketKnowledgeRepository knowledgeRepository, EmbeddingCodec embeddingCodec) {
        this.knowledgeRepository = knowledgeRepository;
        this.embeddingCodec = embeddingCodec;
    }

    public List<RetrievedChunk> retrieveForQuestion(float[] queryEmbedding, String queryText, int topK) {
        List<String> ticketIds = TicketIdQuestionParser.extractTicketIds(queryText);
        if (ticketIds.isEmpty()) {
            return List.of();
        }
        float[] query = EmbeddingCodec.normalize(queryEmbedding);
        List<ScoredChunk> scored = new ArrayList<>();
        for (TicketKnowledge chunk : knowledgeRepository.findEligibleByTicketIds(ticketIds)) {
            float[] vector = EmbeddingCodec.normalize(embeddingCodec.decode(chunk.getEmbeddingPayload()));
            double similarity = EmbeddingCodec.cosineSimilarity(query, vector);
            scored.add(new ScoredChunk(
                    chunk.getTicketId(), chunk.getContent(), similarity, chunk.getTicketVersion()));
        }
        scored.sort(Comparator.comparingDouble(ScoredChunk::similarity).reversed());
        return scored.stream()
                .limit(topK)
                .map(s -> new RetrievedChunk(s.ticketId(), s.content(), s.similarity(), s.ticketVersion()))
                .toList();
    }

    private record ScoredChunk(String ticketId, String content, double similarity, long ticketVersion) {}
}
