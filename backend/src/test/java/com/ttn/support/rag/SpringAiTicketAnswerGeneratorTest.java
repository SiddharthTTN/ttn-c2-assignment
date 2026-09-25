package com.ttn.support.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;

class SpringAiTicketAnswerGeneratorTest {

    private SpringAiTicketAnswerGenerator generator;
    private ChatModel chatModel;

    @BeforeEach
    void setUp() {
        chatModel = mock(ChatModel.class);
        generator = new SpringAiTicketAnswerGenerator(chatModel, new ObjectMapper());
    }

    @Test
    void failsClosedOnMalformedModelOutput() {
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("not json at all")))));

        var answer = generator.generate(
                "question", List.of(new RetrievedChunk("TKT-1", "context", 0.9, 1)), Set.of("TKT-1"));

        assertEquals("no relevant tickets found", answer.answer());
        assertTrue(answer.ticketIds().isEmpty());
    }

    @Test
    void parsesValidJson() {
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(
                        new AssistantMessage("{\"answer\":\"fixed\",\"ticketIds\":[\"TKT-1\"]}")))));

        var answer = generator.generate(
                "question", List.of(new RetrievedChunk("TKT-1", "context", 0.9, 1)), Set.of("TKT-1"));

        assertEquals("fixed", answer.answer());
        assertEquals(List.of("TKT-1"), answer.ticketIds());
    }
}
