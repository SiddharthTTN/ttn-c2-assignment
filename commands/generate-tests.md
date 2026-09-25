# Generate tests

Add or extend automated tests that lock behavior to `spec/test-strategy.md` and `spec/api-contract.md`.

## Before writing tests

1. Read the behavior section in the relevant spec file.
2. Check existing tests:
   - `backend/src/test/java/com/ttn/support/web/TicketContractTest.java`
   - `TicketApiIntegrationTest`, `StatusTransitionMatrixTest`, `AskApiIntegrationTest`
   - `frontend` Vitest files under `src/`
3. Prefer extending contract tests for HTTP JSON shape changes.

## Backend patterns

- `@SpringBootTest(properties = "spring.profiles.active=test")` + `@AutoConfigureMockMvc` for HTTP.
- Use `jsonPath` for field presence/absence (e.g. comment response must not include `comments` array).
- Name tests after behavior: `patchClearsNullableFieldsAndRejectsEmptyBody`, not `testPatch1`.
- For forbidden enums, assert **400** and optional `details` content.

## Frontend patterns

- Testing Library: query by role/label per `spec/ui-flow.md`.
- Cover both empty states for ticket list.
- Mock API with realistic JSON matching backend DTOs.

## RAG tests

- Deterministic fakes in test profile — do not require Ollama in unit/contract CI.
- Assert `noRelevantTickets`, `ticketIds` ordering/uniqueness, and no-match message text from spec.

## Commands

```bash
make backend-test
make frontend-test
# targeted:
cd backend && mvn -q test -Dtest=TicketContractTest
```

## Deliverable

- New/updated test classes only in `backend/src/test` or `frontend/src` (when implementation task allows).
- Short comment in PR/description mapping each test to spec bullet.
- If exposing a past AI mistake, reference `docs/ai-mistakes.md`.
