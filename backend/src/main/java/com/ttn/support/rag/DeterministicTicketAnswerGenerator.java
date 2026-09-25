package com.ttn.support.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"h2", "test"})
public class DeterministicTicketAnswerGenerator implements TicketAnswerGenerator {

    private final ObjectMapper objectMapper;

    public DeterministicTicketAnswerGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public GeneratedAnswer generate(String question, List<RetrievedChunk> chunks, Set<String> allowedTicketIds) {
        List<String> cited = rankTicketsByOverlap(question, chunks, allowedTicketIds);
        if (cited.isEmpty()) {
            return new GeneratedAnswer("no relevant tickets found", List.of());
        }
        String context = chunks.stream()
                .filter(chunk -> chunk.ticketId().equals(cited.get(0)))
                .map(RetrievedChunk::content)
                .collect(Collectors.joining("\n"));
        String answer = summarize(context, question);
        return new GeneratedAnswer(answer, List.of(cited.get(0)));
    }

    private List<String> rankTicketsByOverlap(
            String question, List<RetrievedChunk> chunks, java.util.Set<String> allowedTicketIds) {
        java.util.Map<String, Integer> scores = new java.util.LinkedHashMap<>();
        for (String ticketId : allowedTicketIds) {
            String combined = chunks.stream()
                    .filter(chunk -> chunk.ticketId().equals(ticketId))
                    .map(RetrievedChunk::content)
                    .collect(Collectors.joining(" "));
            scores.put(ticketId, overlapScore(question, combined));
        }
        return scores.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(java.util.Map.Entry::getKey)
                .toList();
    }

    static int overlapScore(String left, String right) {
        java.util.Set<String> rightTokens = tokenize(right);
        int score = 0;
        for (String token : tokenize(left)) {
            if (token.length() >= 4 && rightTokens.contains(token)) {
                score++;
            }
        }
        return score;
    }

    private static java.util.Set<String> tokenize(String text) {
        java.util.Set<String> tokens = new java.util.LinkedHashSet<>();
        for (String token : text.toLowerCase(Locale.ROOT).split("\\W+")) {
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private String summarize(String context, String question) {
        String snippet = context.length() > 240 ? context.substring(0, 240) + "..." : context;
        return "Based on retrieved tickets: " + snippet;
    }

    public List<String> parseCitations(String modelOutput, Set<String> allowed) {
        try {
            JsonNode node = objectMapper.readTree(modelOutput);
            List<String> ids = new ArrayList<>();
            if (node.has("ticketIds") && node.get("ticketIds").isArray()) {
                node.get("ticketIds").forEach(n -> ids.add(n.asText()));
            }
            return ids.stream().filter(allowed::contains).distinct().toList();
        } catch (Exception ignored) {
            LinkedHashSet<String> found = new LinkedHashSet<>();
            for (String ticketId : allowed) {
                if (modelOutput.contains(ticketId)) {
                    found.add(ticketId);
                }
            }
            return List.copyOf(found);
        }
    }
}
