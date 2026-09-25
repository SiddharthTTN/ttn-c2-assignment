# API contract

All endpoints are unauthenticated JSON under `/api`. The standard client-error body is:

```json
{"error":"Human-readable summary","details":["Field or action detail"]}
```

Endpoints:

- `POST /api/tickets` creates an OPEN ticket from title, description, priority, optional assignee/category/resolutionNotes; returns 201 or 400.
- `GET /api/tickets?q=&status=` returns `{"items":[Ticket]}`; invalid status returns 400.
- `GET /api/tickets/{id}` returns a ticket with chronological comments; returns 404 when absent.
- `PATCH /api/tickets/{id}` updates any subset of editable fields, never status; returns 200, 400, or 404.
- `POST /api/tickets/{id}/status` accepts `{"status":"..."}`; returns 200, 400 for invalid enum, 404, or 409 for forbidden transition.
- `POST /api/tickets/{id}/comments` accepts a nonblank `body`; returns 201, 400, or 404.
- `POST /api/ai/ask` accepts a nonblank `question`; returns an answer, retrieved ticket ids, and no-match flag; returns 400 or 503 when model infrastructure is unavailable.
- `GET /swagger` serves interactive OpenAPI documentation.

There is no pagination for the exercise dataset capped at 1,000 tickets. POST operations are not idempotent; browser controls disable repeat submission. PATCH and status requests are semantically idempotent only when the requested resulting state/value is unchanged, but repeated invalid or already-completed transitions still follow state-machine rules.
