package com.ttn.support.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChunkingServiceTest {

    private final ChunkingService chunkingService = new ChunkingService();

    @Test
    void keepsSmallFieldsSingleChunk() {
        assertEquals(1, chunkingService.chunkField("Short description").size());
    }

    @Test
    void splitsLargeFields() {
        StringBuilder large = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            large.append("token").append(i).append(' ');
        }
        assertTrue(chunkingService.chunkField(large.toString()).size() > 1);
    }
}
