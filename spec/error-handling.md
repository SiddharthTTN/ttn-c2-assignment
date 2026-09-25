# Error handling

Controllers map validation failures to 400, missing resources to 404, state conflicts to 409, and unavailable embedding/chat infrastructure to 503. Every client error uses `{error, details[]}` and carries or creates an `X-Correlation-ID`. Unexpected failures return a generic 500 message without stack traces or provider details.

Ticket writes remain committed when post-commit knowledge refresh fails; the ticket is marked `PENDING`, stale chunks are excluded from asks, and a scheduled worker retries with capped exponential backoff. Model generation is not automatically retried to avoid duplicate cost and latency. The UI presents server error meaning next to the affected action and preserves entered form values.
