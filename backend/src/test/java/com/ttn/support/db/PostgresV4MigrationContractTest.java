package com.ttn.support.db;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PostgresV4MigrationContractTest {

    @Test
    void v4BackfillsEmbeddingPayloadFromNativeVectorText() throws Exception {
        String v4 = Files.readString(Path.of("src/main/resources/db/migration/postgres")
                .resolve("V4__embedding_payload_and_retry_schedule.sql"));

        assertTrue(
                v4.contains("embedding::text"),
                "existing vector rows must backfill payload from native embedding text");
        assertFalse(
                v4.contains("embedding_payload = ''"),
                "V4 must not assign empty payload to existing vector rows");
    }
}
