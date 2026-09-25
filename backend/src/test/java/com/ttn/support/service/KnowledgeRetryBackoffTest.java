package com.ttn.support.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ttn.support.config.KnowledgeRetryProperties;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class KnowledgeRetryBackoffTest {

    @Test
    void usesCappedExponentialDelays() {
        KnowledgeRetryProperties properties = new KnowledgeRetryProperties();
        KnowledgeRetryBackoff backoff = new KnowledgeRetryBackoff(properties);
        assertEquals(5_000L, backoff.delayMsForAttempt(1));
        assertEquals(30_000L, backoff.delayMsForAttempt(2));
        assertEquals(300_000L, backoff.delayMsForAttempt(3));
        assertEquals(300_000L, backoff.delayMsForAttempt(99));

        Instant before = Instant.now();
        Instant first = backoff.nextRetryAt(1);
        Duration delta = Duration.between(before, first);
        assertEquals(5, delta.toSeconds());
    }
}
