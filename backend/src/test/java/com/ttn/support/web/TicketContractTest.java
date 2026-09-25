package com.ttn.support.web;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class TicketContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void openApiDocumentIsAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths['/api/tickets']").exists())
                .andExpect(jsonPath("$.components.schemas.UpdateTicketRequest.properties.title").exists())
                .andExpect(jsonPath("$.components.schemas.UpdateTicketRequest.properties.description").exists())
                .andExpect(jsonPath("$.components.schemas.UpdateTicketRequest.properties.priority").exists())
                .andExpect(jsonPath("$.components.schemas.UpdateTicketRequest.properties.assignee").exists())
                .andExpect(jsonPath("$.components.schemas.UpdateTicketRequest.properties.category").exists())
                .andExpect(jsonPath("$.components.schemas.UpdateTicketRequest.properties.resolutionNotes").exists())
                .andExpect(jsonPath("$.components.schemas.UpdateTicketRequest.properties.titlePresent").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.CommentResponse.properties.ticketId").exists());
    }

    @Test
    void postCommentReturnsCommentContract() throws Exception {
        String ticketId = createTicket("Comment contract ticket", "desc", "LOW", "alice", "ops");
        mockMvc.perform(post("/api/tickets/" + ticketId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"First comment body\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.ticketId").value(ticketId))
                .andExpect(jsonPath("$.body").value("First comment body"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.comments").doesNotExist());
    }

    @Test
    void patchClearsNullableFieldsAndRejectsEmptyBody() throws Exception {
        String ticketId = createTicket("Patch ticket", "desc", "MEDIUM", "dana", "payments");
        mockMvc.perform(patch("/api/tickets/" + ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/tickets/" + ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignee\":null,\"category\":null,\"resolutionNotes\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee").isEmpty())
                .andExpect(jsonPath("$.category").isEmpty())
                .andExpect(jsonPath("$.resolutionNotes").isEmpty());
    }

    @Test
    void patchRejectsNullRequiredFieldsAndLengthViolations() throws Exception {
        String ticketId = createTicket("Validation ticket", "desc", "HIGH", null, null);
        mockMvc.perform(patch("/api/tickets/" + ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":null}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/tickets/" + ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":null}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/tickets/" + ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priority\":null}"))
                .andExpect(status().isBadRequest());

        String longTitle = "x".repeat(201);
        mockMvc.perform(patch("/api/tickets/" + ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + longTitle + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsUrgentPriorityAndOversizedDescription() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"title\":\"Urgent\",\"description\":\"desc\",\"priority\":\"URGENT\"}"))
                .andExpect(status().isBadRequest());

        String longDescription = "d".repeat(20001);
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Long desc\",\"description\":\""
                                + longDescription
                                + "\",\"priority\":\"LOW\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasItem(org.hamcrest.Matchers.containsString("description"))));
    }

    private String createTicket(
            String title, String description, String priority, String assignee, String category)
            throws Exception {
        String assigneeJson = assignee == null ? "null" : "\"" + assignee + "\"";
        String categoryJson = category == null ? "null" : "\"" + category + "\"";
        MvcResult result = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\""
                                + title
                                + "\",\"description\":\""
                                + description
                                + "\",\"priority\":\""
                                + priority
                                + "\",\"assignee\":"
                                + assigneeJson
                                + ",\"category\":"
                                + categoryJson
                                + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asText();
    }
}
