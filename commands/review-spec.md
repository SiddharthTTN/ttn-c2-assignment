# Review spec

Validate that `spec/` remains internally consistent, implementable, and aligned with the codebase — without rewriting product scope.

## Inputs

- All files listed in `spec/README.md`
- Root `README.md` and `docs/ai-mistakes.md`
- Backend DTOs/controllers and frontend flows (read-only cross-check)

## Consistency checks

1. **API ↔ data model** — Field names, lengths, enums (`priority`, `status`), nullable columns vs PATCH null rules.
2. **API ↔ state machine** — Status change endpoint matches transition table; PATCH excludes status.
3. **API ↔ RAG** — Ask request/response JSON matches `rag-api-contract.md`; ingestion triggers on same events as `rag-ingestion.md`.
4. **UI ↔ API** — Screens in `ui-flow.md` only call documented endpoints; empty states match list behavior.
5. **Test strategy** — Every normative statement in api-contract has a named test layer (contract/integration/unit).
6. **Integrations** — Ollama model names, 768 dimensions, docker/env docs match `application-postgres.yml` and `.env.example`.

## Drift detection

- OpenAPI (`/v3/api-docs`) vs `api-contract.md` endpoint list
- README commands vs Makefile targets
- Glossary terms used consistently

## Output format

```markdown
## Spec health
Pass | Pass with notes | Fail

## Conflicts / gaps
- [file:section] description → recommended edit

## Suggested spec edits
(prioritized, minimal)

## No change needed
(sections verified OK)
```

Do not edit `spec/` in this command unless the user explicitly asks to apply fixes; default is review report only.
