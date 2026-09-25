---
name: documentation
description: Update living specs, operator docs, and agent hygiene when behavior or runbooks change. Use when editing spec/, README, docs/, or postmortems after review findings.
---

# Documentation skill

Keep **`spec/`** as the behavioral source of truth and root **`README.md`** as the operator entrypoint. Agent rules/commands are steering only — they must not contradict spec.

## When to update what

| Change type | Update |
|-------------|--------|
| New/changed HTTP behavior | `spec/api-contract.md`, `spec/error-handling.md` if errors change, `rules/api-standards.md` if conventions shift |
| Schema / enums | `spec/data-model.md`, Flyway notes in README prerequisites |
| RAG thresholds, ingestion | `spec/rag-ingestion.md`, `spec/rag-api-contract.md`, `rules/rag-vector-store.md` |
| UI flows | `spec/ui-flow.md` |
| Run/install/docker | Root `README.md`, `.env.example`, `docker-compose.yml` comments |
| AI review finding | `docs/ai-mistakes.md` with detection test + fix |
| Prompt / delivery history | `.specstory/history/YYYY-MM-DD-*.md` (concise) |

## Writing standards

- Use complete sentences; link to spec files with relative paths.
- Document **commands** copy-paste ready (bash blocks).
- Never embed real secrets — placeholders only in `.env.example` and compose files.
- Prefer tables for endpoint matrices and environment variables.

## README structure (maintain)

1. Architecture diagram or ASCII overview
2. Prerequisites table
3. H2 demo quick start
4. Postgres/Ollama production profile steps
5. Frontend commands
6. Makefile targets (especially `package` / staged static)
7. Testing (`make test`)
8. Pointer to `spec/README.md` and `docs/ai-mistakes.md`

## Spec cross-links

When editing one spec file, check glossary (`spec/glossary.md`) for term consistency and test strategy for verification obligations.

## Validation after doc-only changes

- `docker compose config` — compose file syntax
- Links in README resolve to existing paths
- If docs claim a Makefile target, run `make help`

## Do not

- Duplicate entire spec sections into README — summarize and link.
- Commit `.env`, `frontend/dist`, or staged `backend/src/main/resources/static/`.
- Change `backend/`, `frontend/`, or `spec/` application sources when the task is hygiene/docs-only unless explicitly requested.
