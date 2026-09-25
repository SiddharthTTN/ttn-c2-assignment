ALTER TABLE ticket ADD COLUMN IF NOT EXISTS knowledge_retry_count BIGINT NOT NULL DEFAULT 0;

ALTER TABLE ticket DROP CONSTRAINT IF EXISTS ticket_knowledge_state_check;
ALTER TABLE ticket ADD CONSTRAINT ticket_knowledge_state_check
    CHECK (knowledge_state IN ('PENDING', 'READY', 'FAILED'));
