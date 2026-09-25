package com.ttn.support.db;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PostgresKnowledgeSchemaContractTest {

    @Test
    void postgresMigrationsKeepNativeVectorHnswAndPayloadColumn() throws Exception {
        Path base = Path.of("src/main/resources/db/migration/postgres");
        String v1 = Files.readString(base.resolve("V1__schema.sql"));
        String v4 = Files.readString(base.resolve("V4__embedding_payload_and_retry_schedule.sql"));

        assertTrue(v1.contains("embedding vector(768)"), "native pgvector column must remain in V1");
        assertTrue(
                v1.contains("USING hnsw (embedding vector_cosine_ops)"),
                "V1 must declare HNSW cosine index on embedding");
        assertTrue(v4.contains("embedding_payload"), "JPA-serialized payload column added in V4");
        assertFalse(v4.contains("DROP COLUMN embedding"), "native vector column must not be dropped");
    }

    @Test
    void knowledgeRefreshNativeInsertWritesPayloadAndVectorColumns() throws Exception {
        Path refreshService = Path.of("src/main/java/com/ttn/support/service/KnowledgeRefreshService.java");
        String source = Files.readString(refreshService);

        assertTrue(
                source.contains("embedding_payload, embedding"),
                "native insert must list both payload and vector columns");
        assertTrue(
                source.contains(":embeddingPayload") && source.contains("CAST(:embedding AS vector)"),
                "native insert must bind serialized payload and pgvector literal");
    }

    @Test
    void pgVectorRetrievalSqlUsesNativeEmbeddingColumn() throws Exception {
        Path retrieval = Path.of("src/main/java/com/ttn/support/rag/PgVectorKnowledgeRetrievalService.java");
        String source = Files.readString(retrieval);
        assertTrue(source.contains("k.embedding <=>"), "retrieval must query native vector column");
        assertFalse(source.contains("embedding_payload"), "retrieval must not depend on payload column");
    }
}
