package com.ttn.support.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ChunkingServiceTest {

    private final ChunkingService chunkingService = new ChunkingService();

    @Test
    void keepsSmallFieldsSingleChunk() {
        assertEquals(1, chunkingService.chunkField("Short description").size());
        assertEquals("Short description", chunkingService.chunkField("  Short description  ").getFirst());
        assertTrue(chunkingService.chunkField(null).isEmpty());
        assertTrue(chunkingService.chunkField("   ").isEmpty());
    }

    @Test
    void splitsLargeFields() {
        StringBuilder large = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            large.append("token").append(i).append(' ');
        }
        assertTrue(chunkingService.chunkField(large.toString()).size() > 1);
    }

    @Test
    void mergesTinyParagraphsAndWindowsLongParagraphs() {
        String tinyParagraphs = "alpha beta\n\ncharlie delta\n\n" + words("long", 510);
        List<String> chunks = chunkingService.chunkField(tinyParagraphs);
        assertTrue(chunks.size() > 1);
        assertTrue(chunks.getFirst().contains("alpha beta\n\ncharlie delta"));

        List<String> oversizedParagraph = chunkingService.chunkField(words("token", 1050));
        assertEquals(3, oversizedParagraph.size());
        assertEquals(500, chunkingService.estimateTokens(oversizedParagraph.getFirst()));
    }

    private static String words(String prefix, int count) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < count; i++) {
            text.append(prefix).append(i).append(' ');
        }
        return text.toString();
    }
}
