package com.ttn.support.rag;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"h2", "test", "pgvector-it"})
public class DeterministicTicketEmbeddingService implements TicketEmbeddingService {

    private final RagProperties ragProperties;

    public DeterministicTicketEmbeddingService(RagProperties ragProperties) {
        this.ragProperties = ragProperties;
    }

    @Override
    public float[] embed(String text) {
        return embedDeterministic(text, ragProperties.getEmbeddingDimensions());
    }

    @Override
    public List<float[]> embedAll(List<String> texts) {
        List<float[]> vectors = new ArrayList<>(texts.size());
        for (String text : texts) {
            vectors.add(embed(text));
        }
        return vectors;
    }

    static float[] embedDeterministic(String text, int dimensions) {
        float[] vector = new float[dimensions];
        String normalized = text.toLowerCase(Locale.ROOT);
        for (String token : normalized.split("\\W+")) {
            if (token.isBlank() || token.length() < 2) {
                continue;
            }
            addToken(vector, token, dimensions);
        }
        for (int i = 0; i + 3 <= normalized.length(); i++) {
            addToken(vector, normalized.substring(i, i + 3), dimensions);
        }
        if (allZero(vector)) {
            vector[0] = 1.0f;
        }
        return EmbeddingCodec.normalize(vector);
    }

    private static void addToken(float[] vector, String token, int dimensions) {
        int bucket = Math.floorMod(token.hashCode(), dimensions);
        vector[bucket] += 1.0f;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] seed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            int secondary = Math.floorMod(seed[0] ^ seed[1], dimensions);
            vector[secondary] += 0.5f;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean allZero(float[] vector) {
        for (float v : vector) {
            if (v != 0f) {
                return false;
            }
        }
        return true;
    }
}
