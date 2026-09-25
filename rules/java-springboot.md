# Java & Spring Boot conventions

Apply when changing code under `backend/`. The living spec in `spec/` wins on behavior; these rules keep implementation consistent.

## Stack

- Java **21**, Spring Boot **3.4.x**, Spring Data JPA, Flyway, Springdoc OpenAPI, Spring AI (Ollama in `postgres` profile).
- Package root: `com.ttn.support`.

## Layering

| Layer | Responsibility |
|-------|----------------|
| `web` | Controllers, DTOs, validation entrypoints, `GlobalExceptionHandler` |
| `service` | Transactions, domain orchestration, status transitions |
| `domain` | Entities, enums, knowledge state |
| `rag` | Chunking, embedding, retrieval, answer generation abstractions |
| `config` | Web, filters, `@ConfigurationProperties` |

Controllers stay thin: validate input, delegate to services, map to response DTOs. Business rules belong in services, not controllers.

## Profiles

- **`h2`** (default): demo database, Ollama autoconfig excluded — use deterministic RAG fakes for tests/demo.
- **`postgres`**: real PostgreSQL + Ollama; Flyway location `db/migration/postgres`.
- **`test`**: used by Surefire (`-Dspring.profiles.active=test`).

Never hardcode credentials; use `application-*.yml` placeholders and environment variables documented in `.env.example`.

## API & DTO rules

- JSON under `/api`; standard errors: `{"error":"...","details":[...]}` (`spec/error-handling.md`).
- Use dedicated response types per endpoint — e.g. comments return a **comment** DTO, not a full ticket (`docs/ai-mistakes.md`).
- PATCH updates use `UpdateTicketRequest` with **field presence** tracking; do not replace with a plain record that loses omit-vs-null semantics.
- Enums (`TicketPriority`, `TicketStatus`) must match DB checks and `spec/data-model.md` exactly — no extra values like `URGENT`.

## Persistence & knowledge

- Ticket writes commit before vector refresh; failed refresh sets knowledge `PENDING` and retries via scheduler (`spec/rag-ingestion.md`).
- Chunk metadata must include ticket id, status, priority, assignee, category, source type/id, hash, version.
- Search: escaped literal substring on title/description (`SearchPatternBuilder`), not unescaped SQL wildcards.

## Logging & observability

- Status transitions: logger `com.ttn.support.transition` at INFO with ticket id and outcome.
- Ask path: logger `com.ttn.support.ask`; propagate `X-Correlation-ID` via `CorrelationIdFilter`.

## Dependencies

- Prefer Spring and existing project abstractions before new libraries.
- Embedding dimension changes require migration + re-index — not a one-line config tweak.

## Before merge

- Run `make backend-test` or `cd backend && mvn test`.
- If API shapes change, update `spec/api-contract.md` and extend `TicketContractTest`.
