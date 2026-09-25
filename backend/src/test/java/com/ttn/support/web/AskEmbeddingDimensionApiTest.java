package com.ttn.support.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ttn.support.rag.KnowledgeRetrievalService;
import com.ttn.support.rag.RetrievedChunk;
import com.ttn.support.rag.TicketAnswerGenerator;
import com.ttn.support.rag.TicketEmbeddingService;
import com.ttn.support.service.KnowledgeRefreshService;
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

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Import(AskEmbeddingDimensionApiTest.DimensionMismatchBeans.class)
class AskEmbeddingDimensionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KnowledgeRefreshService knowledgeRefreshService;

    @BeforeEach
    void refreshSeed() {
        knowledgeRefreshService.refreshTicket("TKT-1001");
    }

    @Test
    void askReturns500WhenEmbeddingDimensionsAreInvalid() throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What caused payment to fail at checkout?\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("An unexpected error occurred"));
    }

    @TestConfiguration
    static class DimensionMismatchBeans {

        @Bean
        @Primary
        TicketEmbeddingService embeddingService() {
            return new TicketEmbeddingService() {
                @Override
                public float[] embed(String text) {
                    throw new IllegalStateException("Embedding dimension mismatch: expected 768 but got 4");
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
