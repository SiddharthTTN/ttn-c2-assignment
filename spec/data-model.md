# Data model

## `ticket`

- `id varchar(32)` primary key, generated from sequence as `TKT-<n>`
- `title varchar(200)` not null
- `description text` not null
- `status varchar(20)` not null, enum check
- `priority varchar(10)` not null, enum check
- `assignee varchar(120)` nullable
- `category varchar(80)` nullable
- `resolution_notes text` nullable
- `knowledge_state varchar(16)` not null default `PENDING`
- `knowledge_version bigint` not null default 0
- `knowledge_retry_count bigint` not null default 0
- `knowledge_next_retry_at timestamptz` nullable; populated only while a failed refresh is waiting for its next capped-backoff attempt
- `created_at timestamptz`, `updated_at timestamptz` not null

Indexes: status, priority, updated_at; enable `pg_trgm` and add GIN trigram indexes on `lower(title)` and `lower(description)`. Keyword search escapes wildcard characters and performs case-insensitive literal substring matching against either field.

## `ticket_comment`

- `id bigint generated always as identity` primary key
- `ticket_id varchar(32)` not null references ticket on delete cascade
- `body text` not null
- `created_at timestamptz` not null

Index: `(ticket_id, created_at, id)`.

## `ticket_knowledge`

- `id uuid` primary key
- `ticket_id varchar(32)` not null references ticket on delete cascade
- `source_type varchar(24)`, `source_id varchar(64)`, `chunk_index integer`
- `content text`, `content_hash char(64)` not null
- `ticket_version bigint` not null
- metadata columns: `status`, `priority`, `assignee`, `category`
- `embedding_payload text` not null; normalized serialization used only by deterministic H2 retrieval and exact-ticket chunk loading
- `embedding vector(768)` not null for the default `nomic-embed-text` profile
- `created_at timestamptz` not null

Unique `(ticket_id, source_type, source_id, chunk_index)`; indexes on ticket id and an HNSW cosine index on embedding. JPA maps `embedding_payload`, not the PostgreSQL vector column; PostgreSQL refresh writes both columns in one native insert and PGVector similarity SQL reads only `embedding`. Changing embedding dimensions requires a matching migration and complete re-index.

Ticket ids are immutable. The single ticket-number row is selected `FOR UPDATE` and incremented in one transaction before formatting `TKT-<n>`. State changes use optimistic locking or a row lock so concurrent transitions cannot bypass the state machine.
