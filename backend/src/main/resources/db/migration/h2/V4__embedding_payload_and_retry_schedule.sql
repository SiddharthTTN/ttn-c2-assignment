ALTER TABLE ticket_knowledge RENAME COLUMN embedding TO embedding_payload;

ALTER TABLE ticket ADD COLUMN knowledge_next_retry_at TIMESTAMP WITH TIME ZONE;
