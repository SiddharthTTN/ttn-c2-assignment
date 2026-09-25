package com.ttn.support.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.Column;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class TicketKnowledgeMappingContractTest {

    @Test
    void mapsDeterministicPayloadOnlyNotPgVectorColumn() throws Exception {
        Field payloadField = TicketKnowledge.class.getDeclaredField("embeddingPayload");
        Column column = payloadField.getAnnotation(Column.class);
        assertNotNull(column);
        assertTrue(column.name().equals("embedding_payload"));

        for (Field field : TicketKnowledge.class.getDeclaredFields()) {
            Column mapping = field.getAnnotation(Column.class);
            if (mapping != null && "embedding".equals(mapping.name())) {
                assertFalse(true, "JPA must not map native embedding vector column");
            }
        }
    }
}
