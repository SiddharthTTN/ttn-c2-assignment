# Review code

Perform a focused implementation review of changed Java/TypeScript sources against the living spec and project rules.

## Inputs

- Git diff or file list under `backend/` and `frontend/`
- `spec/` (especially `api-contract.md`, `state-machine.md`, `rag-ingestion.md`)
- `rules/java-springboot.md`, `rules/api-standards.md`, `rules/testing.md`, `rules/rag-vector-store.md`
- `docs/ai-mistakes.md` (known AI failure patterns)

## Steps

1. **Scope** — List modules touched; ignore generated `target/`, `dist/`, staged static.
2. **Spec alignment** — For each public API or state change, cite the spec section; flag undocumented behavior.
3. **DTO & HTTP** — Verify status codes, error body shape, comment vs ticket responses, PATCH presence/null semantics.
4. **Domain** — Enums match DB; transitions match state machine; no extra priorities/statuses.
5. **RAG** — Version bumps on writes, PENDING/retry path, retrieval thresholds, citation filtering.
6. **Tests** — Require matching updates in contract/integration tests; run `make test` or targeted `TicketContractTest`.
7. **Secrets & config** — No hardcoded credentials; env vars documented in `.env.example`.

## Output format

```markdown
## Summary
(one paragraph)

## Findings
| Severity | File | Issue | Spec/rule | Suggested fix |

## Tests run
(commands + pass/fail)

## Residual risks
(if any)
```

Block merge on any **High** finding (wrong contract, missing transition guard, RAG stale reads, secrets).
