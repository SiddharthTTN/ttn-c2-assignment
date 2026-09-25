# RAG API contract

`POST /api/ai/ask` accepts:

```json
{"question":"What caused previous payment failures?"}
```

A qualifying retrieval and supported generation returns:

```json
{"answer":"Previous failures were caused by ...","ticketIds":["TKT-1001"],"noRelevantTickets":false}
```

No qualifying chunk returns without generation:

```json
{"answer":"no relevant tickets found","ticketIds":[],"noRelevantTickets":true}
```

The question must be nonblank and at most 2,000 characters. Retrieved chunks are limited by configured top-K and similarity cutoff. Returned ticket ids are ordered, unique, and constrained to the retrieved set. Empty/unsupported model citations fail closed as a no-match. Provider timeout or outage returns 503 using the standard error body; the endpoint performs no mutation or tool call.
