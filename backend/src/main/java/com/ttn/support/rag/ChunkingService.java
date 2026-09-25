package com.ttn.support.rag;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ChunkingService {

    private static final int TARGET_TOKENS = 500;
    private static final int OVERLAP_TOKENS = 50;
    private static final int MIN_COMBINE_TOKENS = 80;

    public List<String> chunkField(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalized = text.trim();
        if (estimateTokens(normalized) <= TARGET_TOKENS) {
            return List.of(normalized);
        }
        List<String> paragraphs = splitParagraphs(normalized);
        List<String> merged = mergeTinyParagraphs(paragraphs);
        return windowParagraphs(merged);
    }

    private List<String> splitParagraphs(String text) {
        String[] parts = text.split("\\n\\s*\\n");
        List<String> paragraphs = new ArrayList<>();
        for (String part : parts) {
            String p = part.trim();
            if (!p.isEmpty()) {
                paragraphs.add(p);
            }
        }
        if (paragraphs.isEmpty()) {
            paragraphs.add(text);
        }
        return paragraphs;
    }

    private List<String> mergeTinyParagraphs(List<String> paragraphs) {
        if (paragraphs.size() <= 1) {
            return paragraphs;
        }
        List<String> merged = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (buffer.isEmpty()) {
                buffer.append(paragraph);
                continue;
            }
            if (estimateTokens(buffer.toString()) < MIN_COMBINE_TOKENS
                    && estimateTokens(paragraph) < MIN_COMBINE_TOKENS) {
                buffer.append("\n\n").append(paragraph);
            } else {
                merged.add(buffer.toString());
                buffer = new StringBuilder(paragraph);
            }
        }
        if (!buffer.isEmpty()) {
            merged.add(buffer.toString());
        }
        return merged;
    }

    private List<String> windowParagraphs(List<String> paragraphs) {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (current.isEmpty()) {
                current.append(paragraph);
                if (estimateTokens(current.toString()) >= TARGET_TOKENS) {
                    chunks.addAll(splitByTokens(current.toString()));
                    current = new StringBuilder();
                }
                continue;
            }
            String candidate = current + "\n\n" + paragraph;
            if (estimateTokens(candidate) <= TARGET_TOKENS) {
                current = new StringBuilder(candidate);
            } else {
                chunks.add(current.toString());
                String tail = tailTokens(current.toString(), OVERLAP_TOKENS);
                current = new StringBuilder(tail);
                if (!tail.isEmpty()) {
                    current.append("\n\n");
                }
                current.append(paragraph);
                if (estimateTokens(current.toString()) >= TARGET_TOKENS) {
                    chunks.addAll(splitByTokens(current.toString()));
                    current = new StringBuilder();
                }
            }
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }
        return chunks;
    }

    private List<String> splitByTokens(String text) {
        List<String> words = List.of(text.split("\\s+"));
        List<String> parts = new ArrayList<>();
        int start = 0;
        while (start < words.size()) {
            int end = Math.min(words.size(), start + TARGET_TOKENS);
            parts.add(String.join(" ", words.subList(start, end)));
            if (end >= words.size()) {
                break;
            }
            start = Math.max(0, end - OVERLAP_TOKENS);
        }
        return parts;
    }

    private String tailTokens(String text, int tokenCount) {
        String[] words = text.split("\\s+");
        if (words.length <= tokenCount) {
            return text;
        }
        return String.join(" ", List.of(words).subList(words.length - tokenCount, words.length));
    }

    int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}
