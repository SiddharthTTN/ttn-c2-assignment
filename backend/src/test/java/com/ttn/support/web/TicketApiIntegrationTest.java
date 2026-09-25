package com.ttn.support.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttn.support.domain.TicketPriority;
import com.ttn.support.domain.TicketStatus;
import com.ttn.support.web.dto.CreateTicketRequest;
import com.ttn.support.web.dto.StatusChangeRequest;
import com.ttn.support.web.dto.UpdateTicketRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class TicketApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerEndpointRedirects() throws Exception {
        mockMvc.perform(get("/swagger")).andExpect(status().is3xxRedirection());
    }

    @Test
    void createsListsGetsUpdatesCommentsAndTransitions() throws Exception {
        CreateTicketRequest create = new CreateTicketRequest();
        create.setTitle("Searchable Payment issue");
        create.setDescription("Payment gateway timeout during checkout");
        create.setPriority(TicketPriority.HIGH);
        create.setAssignee("dana");
        create.setCategory("payments");

        MvcResult created = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(containsString("TKT-")))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(header().exists("X-Correlation-ID"))
                .andReturn();

        String id = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id")
                .asText();

        mockMvc.perform(get("/api/tickets").param("q", "payment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/api/tickets").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id=='" + id + "')]").exists());

        mockMvc.perform(get("/api/tickets").param("status", "NOT_A_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        UpdateTicketRequest update = new UpdateTicketRequest();
        update.setTitle("Updated payment issue");
        mockMvc.perform(patch("/api/tickets/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated payment issue"));

        mockMvc.perform(post("/api/tickets/" + id + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Investigating logs\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comments", hasSize(1)));

        transition(id, TicketStatus.IN_PROGRESS);
        transition(id, TicketStatus.RESOLVED);
        transition(id, TicketStatus.CLOSED);

        StatusChangeRequest reopen = new StatusChangeRequest();
        reopen.setStatus(TicketStatus.OPEN);
        mockMvc.perform(post("/api/tickets/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reopen)))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/tickets/does-not-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidCreatePayload() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"x\",\"priority\":\"HIGH\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    private void transition(String id, TicketStatus status) throws Exception {
        StatusChangeRequest request = new StatusChangeRequest();
        request.setStatus(status);
        mockMvc.perform(post("/api/tickets/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(status.name()));
    }
}
