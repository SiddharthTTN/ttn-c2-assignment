# Test strategy

- Unit tests: validators, ticket-id formatting, every state transition pair, chunking, no-match threshold, citation intersection, and error mapping.
- Repository tests: JPA constraints, chronological comments, full-text search, status filter, and restart persistence.
- Integration tests with PostgreSQL/PGVector Testcontainers: migrations, vector replacement, stale-version exclusion, configured top-K/cutoff, concurrent transitions, and refresh retry.
- Contract tests: every API success/error shape and `/swagger`.
- RAG evaluation: deterministic fake models plus a seed corpus covering payment, TKT-1001 resolution, shipment tracking, high-priority payment, unsupported questions, and attempted unsupported citations.
- Frontend tests: list/create/details/edit/comment/status/ask paths, error association, keyboard navigation, and empty/no-match states.
- Performance checks: p95 ticket operations under 500 ms at 1,000 tickets and ask under 8 seconds in the selected local profile.
- Hygiene checks: required steering/spec/history files, secret scan, and a documented real AI mistake before completion.
