package com.ttn.support.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.domain.Ticket;
import com.ttn.support.rag.TicketEmbeddingService;
import com.ttn.support.repository.TicketRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(properties = "spring.profiles.active=test")
@TestPropertySource(properties = "app.knowledge-retry.max-attempts=2")
@Import(KnowledgeFailureStateTest.FailingEmbeddingConfig.class)
class KnowledgeFailureStateTest {

    @Autowired
    private KnowledgeRefreshService knowledgeRefreshService;

    @Autowired
    private TicketRepository ticketRepository;

    @TestConfiguration
    static class FailingEmbeddingConfig {

        @Bean
        @Primary
        TicketEmbeddingService failingTicketEmbeddingService() {
            return new TicketEmbeddingService() {
                @Override
                public float[] embed(String text) {
                    throw new RuntimeException("embedding down");
                }

                @Override
                public List<float[]> embedAll(List<String> texts) {
                    throw new RuntimeException("embedding down");
                }
            };
        }
    }

    @Test
    void marksKnowledgeFailedAfterRetryBudgetExhausted() {
        assertThrows(RuntimeException.class, () -> knowledgeRefreshService.refreshTicket("TKT-1001"));
        Ticket afterFirst = ticketRepository.findById("TKT-1001").orElseThrow();
        assertEquals(KnowledgeState.PENDING, afterFirst.getKnowledgeState());
        assertEquals(1, afterFirst.getKnowledgeRetryCount());

        assertThrows(RuntimeException.class, () -> knowledgeRefreshService.refreshTicket("TKT-1001"));
        Ticket afterSecond = ticketRepository.findById("TKT-1001").orElseThrow();
        assertEquals(KnowledgeState.FAILED, afterSecond.getKnowledgeState());
        assertEquals(2, afterSecond.getKnowledgeRetryCount());
    }
}
