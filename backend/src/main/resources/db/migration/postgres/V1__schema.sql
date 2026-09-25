CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE ticket_id_sequence (
    next_val BIGINT NOT NULL
);

INSERT INTO ticket_id_sequence (next_val) VALUES (1004);

CREATE TABLE ticket (
    id VARCHAR(32) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED')),
    priority VARCHAR(10) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    assignee VARCHAR(120),
    category VARCHAR(80),
    resolution_notes TEXT,
    knowledge_state VARCHAR(16) NOT NULL DEFAULT 'PENDING' CHECK (knowledge_state IN ('PENDING', 'READY')),
    knowledge_version BIGINT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_ticket_status ON ticket (status);
CREATE INDEX idx_ticket_priority ON ticket (priority);
CREATE INDEX idx_ticket_updated_at ON ticket (updated_at);
CREATE INDEX idx_ticket_title_trgm ON ticket USING gin (lower(title) gin_trgm_ops);
CREATE INDEX idx_ticket_description_trgm ON ticket USING gin (lower(description) gin_trgm_ops);

CREATE TABLE ticket_comment (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_id VARCHAR(32) NOT NULL REFERENCES ticket (id) ON DELETE CASCADE,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_ticket_comment_ticket_created ON ticket_comment (ticket_id, created_at, id);

CREATE TABLE ticket_knowledge (
    id VARCHAR(36) PRIMARY KEY,
    ticket_id VARCHAR(32) NOT NULL REFERENCES ticket (id) ON DELETE CASCADE,
    source_type VARCHAR(24) NOT NULL,
    source_id VARCHAR(64) NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    ticket_version BIGINT NOT NULL,
    status VARCHAR(20),
    priority VARCHAR(10),
    assignee VARCHAR(120),
    category VARCHAR(80),
    embedding vector(768) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_knowledge_chunk UNIQUE (ticket_id, source_type, source_id, chunk_index)
);

CREATE INDEX idx_ticket_knowledge_ticket ON ticket_knowledge (ticket_id);
CREATE INDEX idx_ticket_knowledge_embedding ON ticket_knowledge USING hnsw (embedding vector_cosine_ops);
