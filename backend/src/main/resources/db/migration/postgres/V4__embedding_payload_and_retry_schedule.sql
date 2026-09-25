ALTER TABLE ticket_knowledge ADD COLUMN embedding_payload TEXT;

UPDATE ticket_knowledge SET embedding_payload = '' WHERE embedding_payload IS NULL;

ALTER TABLE ticket_knowledge ALTER COLUMN embedding_payload SET NOT NULL;

ALTER TABLE ticket ADD COLUMN knowledge_next_retry_at TIMESTAMPTZ;
