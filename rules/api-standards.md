# API standards

Source of truth: **`spec/api-contract.md`**, **`spec/error-handling.md`**, **`spec/state-machine.md`**. OpenAPI is served at `/swagger` (redirect to Springdoc UI).

## Base conventions

- Base path: `/api`
- Content-Type: `application/json`
- Authentication: none (exercise scope — see `spec/security.md` for trust boundaries)
- Correlation: `X-Correlation-ID` on responses when filter applies

## Standard error body

```json
{
  "error": "Human-readable summary",
  "details": ["Optional field- or action-specific detail"]
}
```

| HTTP | When |
|------|------|
| 400 | Validation, invalid enum, empty PATCH body, malformed JSON |
| 404 | Unknown ticket id |
| 409 | Forbidden status transition |
| 503 | Model/embedding infrastructure unavailable (`POST /api/ai/ask`) |
| 500 | Unexpected server error (no stack trace in body) |

## Endpoints (summary)

| Method | Path | Success | Notes |
|--------|------|---------|-------|
| POST | `/api/tickets` | 201 + ticket | Priority ∈ {LOW, MEDIUM, HIGH} only |
| GET | `/api/tickets` | 200 `{items:[]}` | `q` substring search; optional `status` filter |
| GET | `/api/tickets/{id}` | 200 + comments | 404 if missing |
| PATCH | `/api/tickets/{id}` | 200 + ticket | Partial update; **no status**; see PATCH rules below |
| POST | `/api/tickets/{id}/status` | 200 | Body `{"status":"..."}`; 409 on illegal transition |
| POST | `/api/tickets/{id}/comments` | 201 + **comment** | Not full ticket — see `CommentResponse` |
| POST | `/api/ai/ask` | 200 | Grounded answer contract — `spec/rag-api-contract.md` |

## PATCH semantics (critical)

Partial update semantics:

1. **Omitted field** → leave existing value unchanged.
2. **Explicit JSON `null`** on nullable fields (`assignee`, `category`, `resolutionNotes`) → clear field.
3. **Explicit JSON `null`** on required fields (`title`, `description`, `priority`) → **400**.
4. **Empty object `{}`** → **400** (“at least one field”).

Implementation uses presence flags + custom deserializer — do not regress to plain `@JsonIgnoreProperties(ignoreUnknown = true)` DTOs without presence tracking.

## Priority and enums

- `TicketPriority`: `LOW`, `MEDIUM`, `HIGH` — reject unknown values at parse time (400).
- `TicketStatus`: transitions only as defined in state machine table.

## Idempotency (documented behavior)

- POST create/comment/ask: not idempotent.
- PATCH/status: semantically idempotent when resulting state unchanged; invalid transitions still error per rules.

## Pagination

None for exercise dataset (≤ 1,000 tickets).

## Swagger / OpenAPI

- JSON spec: `/v3/api-docs`
- UI: `/swagger-ui/index.html`, shortcut redirect `/swagger`
- Contract test must keep `/v3/api-docs` paths in sync with implemented controllers.

## Review checklist

- [ ] Response DTO matches endpoint (comment vs ticket)
- [ ] Error shape matches `ApiErrorResponse`
- [ ] PATCH omit vs null covered in `TicketContractTest`
- [ ] Ask response includes `answer`, `ticketIds`, `noRelevantTickets`
- [ ] Spec updated if behavior intentionally changes
