package com.ttn.support.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.profiles.active=test")
class TicketIdSequenceConcurrencyTest {

    @Autowired
    private TicketIdGenerator ticketIdGenerator;

    @Test
    void assignsUniqueIdsUnderConcurrency() throws Exception {
        int threads = 8;
        int perThread = 5;
        Set<String> ids = ConcurrentHashMap.newKeySet();
        try (ExecutorService pool = Executors.newFixedThreadPool(threads)) {
            var futures = IntStream.range(0, threads)
                    .mapToObj(i -> pool.submit(() -> {
                        for (int j = 0; j < perThread; j++) {
                            ids.add(ticketIdGenerator.nextId());
                        }
                        return null;
                    }))
                    .toList();
            for (Future<?> future : futures) {
                future.get();
            }
        }
        assertEquals(threads * perThread, ids.size());
    }
}
