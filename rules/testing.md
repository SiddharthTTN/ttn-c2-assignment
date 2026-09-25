# Testing rules

Align tests with `spec/test-strategy.md`. Prefer behavior that proves contracts and state rules, not snapshot noise.

## Backend (JUnit 5 + Spring Boot Test)

| Layer | Location / examples | Focus |
|-------|---------------------|--------|
| Unit | `service/*Test`, `rag/ChunkingServiceTest` | Pure logic: transitions, chunk boundaries, search escaping |
| Web integration | `TicketApiIntegrationTest`, `AskApiIntegrationTest` | End-to-end HTTP with test profile |
| Contract | `TicketContractTest` | Every success/error JSON shape, OpenAPI doc, PATCH null semantics, invalid enums |
| Matrix | `StatusTransitionMatrixTest` | All allowed/forbidden status transitions |

**Profile:** Surefire runs with `-Dspring.profiles.active=test` (see root `backend/pom.xml`).

### Contract tests are mandatory for API changes

When adding or changing endpoints:

1. Assert status code and JSON paths (`jsonPath`).
2. Assert error `details` when spec requires field-level messages.
3. Keep `/v3/api-docs` and `/swagger` redirect behavior covered.

Reference mistakes caught by contracts: `docs/ai-mistakes.md`.

### Integration vs unit

- Use `@SpringBootTest` + `MockMvc` for HTTP contracts.
- Do not require Docker/Ollama for default CI — test profile uses in-memory/deterministic RAG implementations.
- PostgreSQL/Testcontainers scenarios belong in integration tests when explicitly added; document profile requirements in test class Javadoc.

## Frontend (Vitest + Testing Library)

- Run via `cd frontend && npm test` or root `make frontend-test`.
- Cover list, create, detail, edit, comment, status, ask flows per `spec/ui-flow.md`.
- Assert accessible labels, error association, empty states (“No tickets yet” vs “No tickets match…”).

## Root commands

```bash
make test              # backend + frontend
make backend-test      # mvn test only
make frontend-test     # vitest run
```

## RAG evaluation

Seed corpus and question sets per `spec/evaluation-strategy.md`. Tests should include:

- Payment / TKT-1001 resolution scenarios
- No-match when similarity below threshold
- Citation intersection (model cannot cite tickets outside retrieval set)

## Hygiene

- No secrets in test resources.
- No committed `frontend/dist` or staged `backend/src/main/resources/static/`.
- Document new AI mistake patterns in `docs/ai-mistakes.md` when review finds them.
