package com.ttn.support.rag;

import java.util.List;

public interface TicketEmbeddingService {

    float[] embed(String text);

    List<float[]> embedAll(List<String> texts);
}
