package com.ttn.support.rag;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class EmbeddingCodecTest {

    private final EmbeddingCodec codec = new EmbeddingCodec(new ObjectMapper());

    @Test
    void encodesAndDecodesVectors() {
        float[] vector = {1.0f, -2.5f};
        assertArrayEquals(vector, codec.decode(codec.encode(vector)));
        assertThrows(IllegalStateException.class, () -> codec.decode("not-json"));
    }

    @Test
    void normalizesVectorsAndPreservesZeroVector() {
        float[] zero = {0.0f, 0.0f};
        assertSame(zero, EmbeddingCodec.normalize(zero));
        assertArrayEquals(new float[] {0.6f, 0.8f}, EmbeddingCodec.normalize(new float[] {3.0f, 4.0f}), 0.0001f);
    }

    @Test
    void calculatesCosineAndRejectsDimensionMismatch() {
        assertEquals(11.0, EmbeddingCodec.cosineSimilarity(new float[] {1, 2}, new float[] {3, 4}));
        assertThrows(
                IllegalArgumentException.class,
                () -> EmbeddingCodec.cosineSimilarity(new float[] {1}, new float[] {1, 2}));
    }

    @Test
    void formatsAndParsesPgVectorLiterals() {
        assertEquals("[1.0,-2.5]", EmbeddingCodec.toPgVectorLiteral(new float[] {1.0f, -2.5f}));
        assertArrayEquals(new float[] {1.0f, -2.5f}, EmbeddingCodec.parsePgVector(" [1.0, -2.5] "));
        assertArrayEquals(new float[] {1.0f, 2.0f}, EmbeddingCodec.parsePgVector("1.0,2.0"));
        assertArrayEquals(new float[0], EmbeddingCodec.parsePgVector("[]"));
    }
}
