package com.ttn.support.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.repository.TicketKnowledgeRepository;
import com.ttn.support.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.profiles.active=test")
class KnowledgeRefreshServiceTest {

    @Autowired
    private KnowledgeRefreshService knowledgeRefreshService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketKnowledgeRepository knowledgeRepository;

    @Test
    void refreshReplacesChunksAndMarksTicketReady() {
        knowledgeRefreshService.refreshTicket("TKT-1002");
        var ticket = ticketRepository.findById("TKT-1002").orElseThrow();
        assertEquals(KnowledgeState.READY, ticket.getKnowledgeState());
        assertTrue(knowledgeRepository.findByTicketId("TKT-1002").size() >= 1);
    }
}
