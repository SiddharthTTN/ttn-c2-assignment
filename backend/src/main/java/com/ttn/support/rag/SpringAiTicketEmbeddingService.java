package com.ttn.support.rag;

import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("postgres & !pgvector-it")
public class SpringAiTicketEmbeddingService implements TicketEmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final RagProperties ragProperties;

    public SpringAiTicketEmbeddingService(EmbeddingModel embeddingModel, RagProperties ragProperties) {
        this.embeddingModel = embeddingModel;
        this.ragProperties = ragProperties;
    }

    @Override
    public float[] embed(String text) {
        try {
            return validateDimensions(embeddingModel.embed(text));
        } catch (RuntimeException ex) {
            throw ModelInfrastructureExceptionMapper.toModelUnavailable("embedding", ex);
        }
    }

    @Override
    public List<float[]> embedAll(List<String> texts) {
        try {
            List<float[]> vectors = new ArrayList<>();
            for (float[] vector : embeddingModel.embed(texts)) {
                vectors.add(validateDimensions(vector));
            }
            return vectors;
        } catch (RuntimeException ex) {
            throw ModelInfrastructureExceptionMapper.toModelUnavailable("embedding", ex);
        }
    }

    private float[] validateDimensions(float[] vector) {
        int expected = ragProperties.getEmbeddingDimensions();
        if (vector.length != expected) {
            throw new IllegalStateException(
                    "Embedding dimension mismatch: expected " + expected + " but got " + vector.length);
        }
        return vector;
    }
}
