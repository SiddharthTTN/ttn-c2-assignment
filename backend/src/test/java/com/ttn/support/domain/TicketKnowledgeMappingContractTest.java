package com.ttn.support.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Test
    void mapsPostgresTextColumnsWithoutOidLobs() throws Exception {
        for (Field field : List.of(
                Ticket.class.getDeclaredField("description"),
                Ticket.class.getDeclaredField("resolutionNotes"),
                TicketComment.class.getDeclaredField("body"),
                TicketKnowledge.class.getDeclaredField("content"),
                TicketKnowledge.class.getDeclaredField("embeddingPayload"))) {
            assertFalse(field.isAnnotationPresent(Lob.class));
            JdbcTypeCode jdbcType = field.getAnnotation(JdbcTypeCode.class);
            assertNotNull(jdbcType);
            assertEquals(SqlTypes.LONGVARCHAR, jdbcType.value());
        }
    }
}
