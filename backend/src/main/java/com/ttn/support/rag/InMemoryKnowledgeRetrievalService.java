package com.ttn.support.rag;

import com.ttn.support.domain.TicketKnowledge;
import com.ttn.support.repository.TicketKnowledgeRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"h2", "test"})
public class InMemoryKnowledgeRetrievalService implements KnowledgeRetrievalService {

    private final TicketKnowledgeRepository knowledgeRepository;
    private final EmbeddingCodec embeddingCodec;
    private final ExactTicketChunkRetriever exactTicketChunkRetriever;

    public InMemoryKnowledgeRetrievalService(
            TicketKnowledgeRepository knowledgeRepository,
            EmbeddingCodec embeddingCodec,
            ExactTicketChunkRetriever exactTicketChunkRetriever) {
        this.knowledgeRepository = knowledgeRepository;
        this.embeddingCodec = embeddingCodec;
        this.exactTicketChunkRetriever = exactTicketChunkRetriever;
    }

    @Override
    public List<RetrievedChunk> retrieve(
            float[] queryEmbedding, String queryText, int topK, double similarityThreshold) {
        List<RetrievedChunk> exactMatches =
                exactTicketChunkRetriever.retrieveForQuestion(queryEmbedding, queryText, topK);
        List<RetrievedChunk> similarityMatches = retrieveBySimilarity(queryEmbedding, queryText, topK, similarityThreshold);
        return RetrievedChunkMerger.merge(exactMatches, similarityMatches, topK);
    }

    private List<RetrievedChunk> retrieveBySimilarity(
            float[] queryEmbedding, String queryText, int topK, double similarityThreshold) {
        float[] query = EmbeddingCodec.normalize(queryEmbedding);
        List<Scored> scored = new ArrayList<>();
        for (TicketKnowledge chunk : knowledgeRepository.findAllEligible()) {
            if (!sharesSignificantToken(queryText, chunk.getContent())) {
                continue;
            }
            float[] vector = EmbeddingCodec.normalize(embeddingCodec.decode(chunk.getEmbedding()));
            double similarity = EmbeddingCodec.cosineSimilarity(query, vector);
            if (similarity >= similarityThreshold) {
                scored.add(new Scored(chunk.getTicketId(), chunk.getContent(), similarity, chunk.getTicketVersion()));
            }
        }
        scored.sort(Comparator.comparingDouble(Scored::similarity).reversed());
        return scored.stream()
                .limit(topK)
                .map(s -> new RetrievedChunk(s.ticketId(), s.content(), s.similarity(), s.ticketVersion()))
                .toList();
    }

    private record Scored(String ticketId, String content, double similarity, long ticketVersion) {}

    static boolean sharesSignificantToken(String queryText, String content) {
        if (queryText == null || content == null) {
            return false;
        }
        var contentTokens = tokenize(content);
        for (String token : tokenize(queryText)) {
            if (token.length() >= 4 && contentTokens.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static java.util.Set<String> tokenize(String text) {
        java.util.Set<String> tokens = new java.util.LinkedHashSet<>();
        for (String token : text.toLowerCase(java.util.Locale.ROOT).split("\\W+")) {
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }
}
