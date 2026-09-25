package com.ttn.support.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(name = "ticket")
public class Ticket {

    @Id
    @Column(length = 32)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TicketPriority priority;

    @Column(length = 120)
    private String assignee;

    @Column(length = 80)
    private String category;

    @Lob
    @Column(name = "resolution_notes")
    private String resolutionNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "knowledge_state", nullable = false, length = 16)
    private KnowledgeState knowledgeState = KnowledgeState.PENDING;

    @Column(name = "knowledge_version", nullable = false)
    private long knowledgeVersion;

    @Column(name = "knowledge_retry_count", nullable = false)
    private long knowledgeRetryCount;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
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

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public KnowledgeState getKnowledgeState() {
        return knowledgeState;
    }

    public void setKnowledgeState(KnowledgeState knowledgeState) {
        this.knowledgeState = knowledgeState;
    }

    public long getKnowledgeVersion() {
        return knowledgeVersion;
    }

    public void setKnowledgeVersion(long knowledgeVersion) {
        this.knowledgeVersion = knowledgeVersion;
    }

    public long getKnowledgeRetryCount() {
        return knowledgeRetryCount;
    }

    public void setKnowledgeRetryCount(long knowledgeRetryCount) {
        this.knowledgeRetryCount = knowledgeRetryCount;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
