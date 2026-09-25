package com.ttn.support.rag;

import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("postgres")
public class SpringAiTicketEmbeddingService implements TicketEmbeddingService {

    private final EmbeddingModel embeddingModel;

    public SpringAiTicketEmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public float[] embed(String text) {
        return embeddingModel.embed(text);
    }

    @Override
    public List<float[]> embedAll(List<String> texts) {
        List<float[]> vectors = new ArrayList<>();
        for (float[] vector : embeddingModel.embed(texts)) {
            vectors.add(vector);
        }
        return vectors;
    }
}
