package com.ttn.support.service;

import com.ttn.support.config.KnowledgeRetryProperties;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeRetryBackoff {

    private static final long[] DELAYS_MS = {5_000L, 30_000L, 300_000L};

    private final KnowledgeRetryProperties properties;

    public KnowledgeRetryBackoff(KnowledgeRetryProperties properties) {
        this.properties = properties;
    }

    public Instant nextRetryAt(long failureCount) {
        int index = (int) Math.min(Math.max(failureCount, 1) - 1, DELAYS_MS.length - 1);
        return Instant.now().plus(Duration.ofMillis(DELAYS_MS[index]));
    }

    public long delayMsForAttempt(long failureCount) {
        int index = (int) Math.min(Math.max(failureCount, 1) - 1, DELAYS_MS.length - 1);
        return DELAYS_MS[index];
    }

    public int getMaxAttempts() {
        return properties.getMaxAttempts();
    }
}
