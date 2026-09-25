package com.ttn.support.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("postgres")
public class SpringAiTicketAnswerGenerator implements TicketAnswerGenerator {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public SpringAiTicketAnswerGenerator(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @Override
    public GeneratedAnswer generate(String question, List<RetrievedChunk> chunks, Set<String> allowedTicketIds) {
        String context = chunks.stream()
                .map(c -> "Ticket " + c.ticketId() + ": " + c.content())
                .collect(Collectors.joining("\n\n"));
        String prompt = """
                You answer support questions using ONLY the ticket context below.
                Do not use outside knowledge.
                Respond with JSON only: {"answer":"...","ticketIds":["TKT-..."]}
                ticketIds must be from the retrieved tickets only.

                Question: %s

                Context:
                %s
                """.formatted(question, context);
        String output = chatModel.call(new Prompt(prompt)).getResult().getOutput().getText();
        return parseOutput(output, allowedTicketIds);
    }

    private GeneratedAnswer parseOutput(String output, Set<String> allowed) {
        try {
            JsonNode node = objectMapper.readTree(output.trim());
            String answer = node.path("answer").asText("");
            List<String> ids = new ArrayList<>();
            if (node.has("ticketIds") && node.get("ticketIds").isArray()) {
                node.get("ticketIds").forEach(n -> ids.add(n.asText()));
            }
            List<String> valid = ids.stream().filter(allowed::contains).distinct().toList();
            if (valid.isEmpty() || answer.isBlank()) {
                return new GeneratedAnswer("no relevant tickets found", List.of());
            }
            return new GeneratedAnswer(answer, valid);
        } catch (Exception e) {
            LinkedHashSet<String> found = new LinkedHashSet<>();
            for (String ticketId : allowed) {
                if (output.contains(ticketId)) {
                    found.add(ticketId);
                }
            }
            if (found.isEmpty()) {
                return new GeneratedAnswer("no relevant tickets found", List.of());
            }
            return new GeneratedAnswer(output.trim(), List.copyOf(found));
        }
    }
}
