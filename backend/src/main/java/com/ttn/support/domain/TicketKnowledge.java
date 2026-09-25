package com.ttn.support.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ticket_knowledge")
public class TicketKnowledge {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "ticket_id", nullable = false, length = 32)
    private String ticketId;

    @Column(name = "source_type", nullable = false, length = 24)
    private String sourceType;

    @Column(name = "source_id", nullable = false, length = 64)
    private String sourceId;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(nullable = false)
    private String content;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "ticket_version", nullable = false)
    private long ticketVersion;

    @Column(length = 20)
    private String status;

    @Column(length = 10)
    private String priority;

    @Column(length = 120)
    private String assignee;

    @Column(length = 80)
    private String category;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "embedding_payload", nullable = false)
    private String embeddingPayload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static TicketKnowledge createNew() {
        TicketKnowledge k = new TicketKnowledge();
        k.id = UUID.randomUUID().toString();
        k.createdAt = Instant.now();
        return k;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public long getTicketVersion() {
        return ticketVersion;
    }

    public void setTicketVersion(long ticketVersion) {
        this.ticketVersion = ticketVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEmbeddingPayload() {
        return embeddingPayload;
    }

    public void setEmbeddingPayload(String embeddingPayload) {
        this.embeddingPayload = embeddingPayload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
