package com.ttn.support.rag;

import java.util.List;

public interface KnowledgeRetrievalService {

    List<RetrievedChunk> retrieve(
            float[] queryEmbedding, String queryText, int topK, double similarityThreshold);
}
