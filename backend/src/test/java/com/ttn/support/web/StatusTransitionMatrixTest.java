package com.ttn.support.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttn.support.domain.TicketPriority;
import com.ttn.support.domain.TicketStatus;
import com.ttn.support.web.dto.CreateTicketRequest;
import com.ttn.support.web.dto.StatusChangeRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class StatusTransitionMatrixTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @CsvSource({
        "IN_PROGRESS,200",
        "CANCELLED,200",
        "RESOLVED,409",
        "CLOSED,409",
        "OPEN,409"
    })
    void openTransitions(String target, int expectedStatus) throws Exception {
        String id = createTicket();
        assertTransition(id, TicketStatus.from(target), expectedStatus);
    }

    @ParameterizedTest
    @CsvSource({
        "RESOLVED,200",
        "CANCELLED,200",
        "OPEN,409",
        "IN_PROGRESS,409"
    })
    void inProgressTransitions(String target, int expectedStatus) throws Exception {
        String id = createTicket();
        assertTransition(id, TicketStatus.IN_PROGRESS, 200);
        assertTransition(id, TicketStatus.from(target), expectedStatus);
    }

    private String createTicket() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Transition matrix ticket");
        request.setDescription("desc");
        request.setPriority(TicketPriority.LOW);
        MvcResult result = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asText();
    }

    private void assertTransition(String id, TicketStatus status, int expectedStatus) throws Exception {
        StatusChangeRequest request = new StatusChangeRequest();
        request.setStatus(status);
        mockMvc.perform(post("/api/tickets/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus));
    }
}
