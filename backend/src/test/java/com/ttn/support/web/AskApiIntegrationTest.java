package com.ttn.support.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ttn.support.service.KnowledgeRefreshService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class AskApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KnowledgeRefreshService knowledgeRefreshService;

    @BeforeEach
    void refreshSeedKnowledge() {
        knowledgeRefreshService.refreshTicket("TKT-1001");
        knowledgeRefreshService.refreshTicket("TKT-1002");
        knowledgeRefreshService.refreshTicket("TKT-1003");
    }

    @Test
    void returnsGroundedAnswerWithCitationsForPaymentQuestion() throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What caused payment to fail at checkout?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noRelevantTickets").value(false))
                .andExpect(jsonPath("$.ticketIds[0]").value("TKT-1001"))
                .andExpect(jsonPath("$.answer").isNotEmpty());
    }

    @Test
    void returnsExactNoMatchForUnrelatedQuestion() throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What is the weather on Mars?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noRelevantTickets").value(true))
                .andExpect(jsonPath("$.answer").value("no relevant tickets found"))
                .andExpect(jsonPath("$.ticketIds").isEmpty());
    }

    @Test
    void rejectsBlankQuestion() throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }
}
