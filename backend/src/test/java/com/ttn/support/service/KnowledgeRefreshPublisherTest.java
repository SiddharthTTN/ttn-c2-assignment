package com.ttn.support.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class KnowledgeRefreshPublisherTest {

    @Test
    void dispatchesRefreshWithoutBlockingTheRequestThread() throws Exception {
        KnowledgeRefreshService refreshService = mock(KnowledgeRefreshService.class);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
                    started.countDown();
                    release.await(2, TimeUnit.SECONDS);
                    return null;
                })
                .when(refreshService)
                .refreshTicket("TKT-1");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.initialize();
        try {
            KnowledgeRefreshPublisher publisher = new KnowledgeRefreshPublisher(refreshService, executor);
            long startedAt = System.nanoTime();
            publisher.scheduleAfterCommit("TKT-1");
            long elapsedMillis = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();

            assertTrue(started.await(1, TimeUnit.SECONDS));
            assertTrue(elapsedMillis < 250, "publisher blocked for " + elapsedMillis + "ms");
        } finally {
            release.countDown();
            executor.shutdown();
        }
    }
}
