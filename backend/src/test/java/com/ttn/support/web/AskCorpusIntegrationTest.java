package com.ttn.support.web;

import static org.hamcrest.Matchers.hasItem;
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
class AskCorpusIntegrationTest {

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
    void answersPaymentHistoryQuestion() throws Exception {
        askExpectingTicket("What caused previous payment failures?", "TKT-1001");
    }

    @Test
    void answersExactTicketResolutionQuestion() throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What was the resolution for ticket TKT-1001?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noRelevantTickets").value(false))
                .andExpect(jsonPath("$.ticketIds[0]").value("TKT-1001"))
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("expired merchant")));
    }

    @Test
    void answersShipmentTrackingCauseQuestion() throws Exception {
        askExpectingTicket("What causes shipment tracking to stop updating?", "TKT-1002");
    }

    @Test
    void answersSimilarResolvedPaymentQuestion() throws Exception {
        askExpectingTicket("How were payment checkout issues resolved?", "TKT-1001");
    }

    @Test
    void answersHighPriorityPaymentQuestion() throws Exception {
        askExpectingTicket("Which high priority open payment ticket should we review?", "TKT-1003");
    }

    @Test
    void returnsNoMatchForUnrelatedQuestion() throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"What is the weather on Mars?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noRelevantTickets").value(true))
                .andExpect(jsonPath("$.answer").value("no relevant tickets found"));
    }

    private void askExpectingTicket(String question, String ticketId) throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"" + question + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noRelevantTickets").value(false))
                .andExpect(jsonPath("$.ticketIds", hasItem(ticketId)))
                .andExpect(jsonPath("$.answer").isNotEmpty());
    }
}
