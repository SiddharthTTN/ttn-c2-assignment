package com.ttn.support.rag;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class RetrievedChunkMerger {

    private RetrievedChunkMerger() {}

    public static List<RetrievedChunk> merge(
            List<RetrievedChunk> priorityChunks, List<RetrievedChunk> similarityChunks, int topK) {
        List<RetrievedChunk> merged = new ArrayList<>(topK);
        Set<String> seen = new LinkedHashSet<>();
        appendUnique(merged, priorityChunks, seen, topK);
        appendUnique(merged, similarityChunks, seen, topK);
        return merged;
    }

    private static void appendUnique(
            List<RetrievedChunk> target, List<RetrievedChunk> source, Set<String> seen, int topK) {
        for (RetrievedChunk chunk : source) {
            if (target.size() >= topK) {
                return;
            }
            String key = chunk.ticketId() + "\u0000" + chunk.content();
            if (seen.add(key)) {
                target.add(chunk);
            }
        }
    }
}
