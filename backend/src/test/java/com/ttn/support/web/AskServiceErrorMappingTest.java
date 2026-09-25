package com.ttn.support.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ttn.support.rag.KnowledgeRetrievalService;
import com.ttn.support.rag.RetrievedChunk;
import com.ttn.support.rag.TicketAnswerGenerator;
import com.ttn.support.rag.TicketEmbeddingService;
import com.ttn.support.service.KnowledgeRefreshService;
import com.ttn.support.web.error.ModelUnavailableException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"spring.profiles.active=test", "app.rag.ask-timeout=50ms"})
@AutoConfigureMockMvc
@Import(AskServiceErrorMappingTest.ErrorBeans.class)
class AskServiceErrorMappingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KnowledgeRefreshService knowledgeRefreshService;

    @Autowired
    private ErrorMode errorMode;

    @BeforeEach
    void refreshSeed() {
        knowledgeRefreshService.refreshTicket("TKT-1001");
    }

    @Test
    void returns503OnlyForModelUnavailable() throws Exception {
        errorMode.mode = ErrorMode.Mode.MODEL_DOWN;
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What caused payment to fail at checkout?\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Model infrastructure is unavailable"));
    }

    @Test
    void returns500ForUnexpectedFailures() throws Exception {
        errorMode.mode = ErrorMode.Mode.UNEXPECTED;
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What caused payment to fail at checkout?\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("An unexpected error occurred"));
    }

    @Test
    void returns503WhenTotalAskDeadlineExpires() throws Exception {
        errorMode.mode = ErrorMode.Mode.SLOW;
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What caused payment to fail at checkout?\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Model infrastructure is unavailable"));
    }

    static class ErrorMode {
        enum Mode {
            MODEL_DOWN,
            UNEXPECTED,
            SLOW
        }

        Mode mode = Mode.MODEL_DOWN;
    }

    @TestConfiguration
    static class ErrorBeans {

        @Bean
        ErrorMode errorMode() {
            return new ErrorMode();
        }

        @Bean
        @Primary
        TicketEmbeddingService embeddingService(ErrorMode errorMode) {
            return new TicketEmbeddingService() {
                @Override
                public float[] embed(String text) {
                    if (errorMode.mode == ErrorMode.Mode.MODEL_DOWN) {
                        throw new ModelUnavailableException("down");
                    }
                    if (errorMode.mode == ErrorMode.Mode.SLOW) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            throw new ModelUnavailableException("interrupted", ex);
                        }
                    }
                    throw new IllegalStateException("boom");
                }

                @Override
                public List<float[]> embedAll(List<String> texts) {
                    return List.of();
                }
            };
        }

        @Bean
        @Primary
        KnowledgeRetrievalService retrievalService() {
            return (queryEmbedding, queryText, topK, similarityThreshold) ->
                    List.of(new RetrievedChunk("TKT-1001", "ctx", 0.9, 1));
        }

        @Bean
        @Primary
        TicketAnswerGenerator answerGenerator() {
            return (question, chunks, allowedTicketIds) ->
                    new TicketAnswerGenerator.GeneratedAnswer("answer", List.of("TKT-1001"));
        }
    }
}
