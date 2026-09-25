# AI implementation mistakes (review record)

This document captures **real defects** introduced during AI-assisted implementation and caught before merge. Use it as a checklist when reviewing generated code against `spec/` and contract tests.

## 1. Unsupported `URGENT` ticket priority

| | |
|---|---|
| **Symptom** | Create-ticket accepted or mishandled `"priority":"URGENT"` despite the data model allowing only `LOW`, `MEDIUM`, `HIGH` (`spec/data-model.md`, enum check). |
| **Root cause** | Generated code treated priority as a free-form string or extended the enum without updating migrations/constraints. |
| **Detection** | `TicketContractTest.createRejectsUrgentPriorityAndOversizedDescription` — `POST /api/tickets` with `"URGENT"` must return **400**. |
| **Fix** | Keep `TicketPriority` aligned to three values; invalid enum values fail deserialization/validation with standard error body (`spec/error-handling.md`). Do not add priorities without a spec + Flyway change. |

## 2. Wrong response type for `POST /api/tickets/{id}/comments`

| | |
|---|---|
| **Symptom** | Comment endpoint returned a full **ticket** (including nested `comments` array) instead of a single comment resource. |
| **Root cause** | Controller reused `TicketResponse` for convenience rather than the comment contract. |
| **Detection** | `TicketContractTest.postCommentReturnsCommentContract` asserts **201** with `id`, `ticketId`, `body`, `createdAt`, and **`comments` must not exist** on the payload. |
| **Fix** | Return `CommentResponse` (or equivalent DTO) from `TicketController.addComment`; map only the created comment fields. |

## 3. PATCH: omitted fields vs explicit JSON `null`

| | |
|---|---|
| **Symptom** | PATCH could not clear nullable fields (`assignee`, `category`, `resolutionNotes`) or rejected valid partial updates because Jackson could not distinguish **field absent** from **field set to null**. |
| **Root cause** | Plain DTO + default deserialization: omitted and null both appear as Java `null`, so the service cannot tell “leave unchanged” from “clear”. |
| **Detection** | `TicketContractTest.patchClearsNullableFieldsAndRejectsEmptyBody` — `{}` → **400**; explicit nulls clear fields → **200** with empty strings/nulls in response. `patchRejectsNullRequiredFieldsAndLengthViolations` — `title`/`description`/`priority` null → **400**. |
| **Fix** | Custom `UpdateTicketRequestDeserializer` with per-field **presence flags**; nullable fields clear on explicit JSON `null`; required fields reject null; empty PATCH body rejected. Validator enforces lengths after presence is known. |

## How contract tests fit the strategy

Per `spec/test-strategy.md`, contract tests guard **HTTP shapes and OpenAPI availability** without standing up Ollama. They complement integration tests (`TicketApiIntegrationTest`, `AskApiIntegrationTest`) and unit tests for state transitions and chunking.

When adding endpoints or PATCH semantics, extend contract tests **first** (or in the same change) so regressions in AI-generated controllers/DTOs fail CI immediately.

## Agent workflow

Before marking implementation complete:

1. Run `make test` (or at minimum `cd backend && mvn test -Dtest=TicketContractTest`).
2. Cross-check enums and nullable PATCH rules against `spec/api-contract.md`.
3. Record any new mistake pattern here if review finds one.
