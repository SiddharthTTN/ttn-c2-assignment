package com.ttn.support.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

@ExtendWith(MockitoExtension.class)
class SpringAiTicketEmbeddingServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;

    private RagProperties ragProperties;
    private SpringAiTicketEmbeddingService service;

    @BeforeEach
    void setUp() {
        ragProperties = new RagProperties();
        ragProperties.setEmbeddingDimensions(768);
        service = new SpringAiTicketEmbeddingService(embeddingModel, ragProperties);
    }

    @Test
    void embedRejectsVectorLengthMismatch() {
        when(embeddingModel.embed(anyString())).thenReturn(new float[4]);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.embed("hello"));
        assertEquals("Embedding dimension mismatch: expected 768 but got 4", ex.getMessage());
    }

    @Test
    void embedAllRejectsVectorLengthMismatch() {
        when(embeddingModel.embed(anyList())).thenReturn(List.of(new float[4]));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.embedAll(List.of("a")));
        assertEquals("Embedding dimension mismatch: expected 768 but got 4", ex.getMessage());
    }
}
