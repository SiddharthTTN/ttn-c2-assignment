# Test strategy

- Unit tests: validators, ticket-id formatting, every state transition pair, chunking, no-match threshold, citation intersection, and error mapping.
- Repository tests: JPA constraints, chronological comments, full-text search, status filter, and restart persistence.
- PostgreSQL contract tests: Flyway retains `vector(768)` plus HNSW cosine indexing, the entity excludes the vector column, and the native refresh insert writes both serialized payload and vector values. An executable PostgreSQL/PGVector Testcontainers suite covers migration startup, vector replacement, stale-version exclusion, and configured top-K/cutoff. Environments without Docker skip it. Docker-enabled GitHub Actions now runs the suite to completion.
- Contract tests: every API success/error shape and `/swagger`.
- RAG evaluation: deterministic fake models plus a seed corpus covering payment, TKT-1001 resolution, shipment tracking, high-priority payment, unsupported questions, and attempted unsupported citations.
- Frontend tests: the real data-router route tree covers list/create/details/edit/comment/status/ask paths, post-create and citation navigation, unsaved-change blocking, error association, keyboard navigation, and empty/no-match states.
- Performance checks: a repeatable H2 integration benchmark loads 1,000 tickets and enforces p95 list, details, and update operations under 500 ms. The total ask deadline is tested independently; production PostgreSQL and live Ollama p95 measurements remain required before production sign-off.
- Hygiene checks: required steering/spec/history files, secret scan, and a documented real AI mistake before completion.
