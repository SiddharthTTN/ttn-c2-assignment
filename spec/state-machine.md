# Ticket state machine

Allowed transitions:

| From | To |
|---|---|
| OPEN | IN_PROGRESS |
| OPEN | CANCELLED |
| IN_PROGRESS | RESOLVED |
| IN_PROGRESS | CANCELLED |
| RESOLVED | CLOSED |

All other transitions, including self-transitions and reopening CLOSED, RESOLVED, or CANCELLED tickets, return HTTP 409 and leave state unchanged. The backend checks the rule while holding an optimistic or row-level concurrency guard. Every attempt logs ticket id, previous state, requested state, and accepted/rejected outcome.
