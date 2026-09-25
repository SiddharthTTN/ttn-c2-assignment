# Test strategy

- Unit tests: validators, ticket-id formatting, every state transition pair, chunking, no-match threshold, citation intersection, and error mapping.
- Repository tests: JPA constraints, chronological comments, full-text search, status filter, and restart persistence.
- PostgreSQL contract tests: Flyway retains `vector(768)` plus HNSW cosine indexing, the entity excludes the vector column, and the native refresh insert writes both serialized payload and vector values. Live PostgreSQL/PGVector Testcontainers coverage for migration startup, vector replacement, stale-version exclusion, and configured top-K/cutoff remains required before production sign-off.
- Contract tests: every API success/error shape and `/swagger`.
- RAG evaluation: deterministic fake models plus a seed corpus covering payment, TKT-1001 resolution, shipment tracking, high-priority payment, unsupported questions, and attempted unsupported citations.
- Frontend tests: the real data-router route tree covers list/create/details/edit/comment/status/ask paths, post-create and citation navigation, unsaved-change blocking, error association, keyboard navigation, and empty/no-match states.
- Performance checks: p95 ticket operations under 500 ms at 1,000 tickets and ask under 8 seconds in the selected local profile.
- Hygiene checks: required steering/spec/history files, secret scan, and a documented real AI mistake before completion.
