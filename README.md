# Grounded Support Ticket System

Monorepo for a grounded support-ticket application: a **Java 21 / Spring Boot** backend, **React / TypeScript (Vite)** frontend, **PostgreSQL + PGVector** for durable relational and vector data, and **Spring AI + Ollama** for embeddings and chat in the production profile. The living specification in [`spec/`](spec/README.md) is the implementation gate.

## Architecture

```text
Browser ──► React SPA (dev: Vite proxy /api) ──► Spring Boot JSON API
                                                      │
                    ┌─────────────────────────────────┼──────────────────────┐
                    ▼                                 ▼                      ▼
              Ticket + comments              PGVector chunks           Ollama models
              (Flyway migrations)            (768-dim embeddings)      (embed + chat)
```

- **Default demo profile (`h2`)**: file-backed H2, deterministic in-process RAG substitutes — no Docker required.
- **Production profile (`postgres`)**: PostgreSQL with pgvector extension, real Ollama embedding/chat models, synchronous ticket knowledge refresh with bounded retry (`PENDING` state).
- **API surface**: unauthenticated JSON under `/api`; OpenAPI UI at [`/swagger`](http://localhost:8080/swagger) (redirects to Springdoc).
- **Ask flow**: embed question → similarity retrieval with configured top-K and threshold → grounded generation with citation validation (see `spec/rag-api-contract.md`).

Authoritative design detail: `spec/architecture.md`, `spec/api-contract.md`, `spec/data-model.md`, `spec/rag-ingestion.md`.

## Prerequisites

| Component | Version / notes |
|-----------|-----------------|
| JDK | 21 |
| Maven | Bundled wrapper (`backend/mvnw`); no system Maven required |
| Node.js | 20+ (for frontend) |
| Docker | Optional — required for `postgres` profile stack |
| Ollama models | `nomic-embed-text`, `llama3.2:3b` (via `make ollama-pull`) |

Copy environment placeholders (no secrets committed):

```bash
cp .env.example .env
# Edit .env locally; never commit .env
```

## Quick start — H2 demo (backend only)

From the repository root:

```bash
make run-h2
```

Backend listens on `http://localhost:8080` (override with `PORT`). Swagger: `http://localhost:8080/swagger`.

Run the frontend against the same API (separate terminal):

```bash
cd frontend && npm ci && npm run dev
```

Vite proxies `/api` to port 8080 (`frontend/vite.config.ts`).

## Production profile — PostgreSQL, PGVector, Ollama

1. Start dependencies:

```bash
make docker-up
make ollama-pull
```

2. Configure `.env` (from `.env.example`) with `SPRING_PROFILES_ACTIVE=postgres` and database/Ollama URLs.

3. Run the backend:

```bash
make run-postgres
```

Flyway applies `classpath:db/migration/postgres`. Embedding dimension defaults to **768** (`app.rag.embedding-dimensions`); changing models/dimensions requires migration and re-indexing per `spec/integrations.md`.

## Frontend commands

```bash
cd frontend
npm ci
npm run dev        # local dev server + API proxy
npm run build      # production bundle → frontend/dist
npm run test       # Vitest
npm run lint       # ESLint
npm run typecheck  # tsc --noEmit
npm run preview    # preview production build
```

## Root Makefile

| Target | Purpose |
|--------|---------|
| `make install` | `npm ci` in frontend |
| `make test` | Backend `mvn test` + frontend Vitest |
| `make build` | Frontend production build |
| `make package` / `make prod-build` | Build frontend, stage `dist` into gitignored `backend/src/main/resources/static/`, then `mvn package` |
| `make clean` | Remove staged static, `frontend/dist`, `backend/target` |
| `make docker-up` / `make docker-down` | Compose stack |
| `make ollama-pull` | Pull default models into the Ollama container |

Staged static assets are **generated** and listed in `.gitignore`; they are not committed.

## Tests

```bash
make test
```

Backend tests use the bundled Maven wrapper and the `test` Spring profile (see `backend/pom.xml` surefire `argLine`). Layers match `spec/test-strategy.md`: unit, web integration, contract tests (`TicketContractTest`), state matrix, RAG chunking, and ask integration with deterministic fakes.

## Repository hygiene for agents

| Path | Role |
|------|------|
| `rules/` | Coding and API conventions for Java/Spring, tests, API, RAG |
| `commands/` | Review and generation prompts for agents |
| `skills/documentation/` | How to update specs and operator docs |
| `docs/ai-mistakes.md` | Real AI implementation mistakes caught in review |

## Security

No credentials belong in git. Use `.env` locally and placeholder values in `.env.example` / `docker-compose.yml`. See `spec/security.md`.

## License

Internal exercise / assignment codebase — see repository owner for terms.
