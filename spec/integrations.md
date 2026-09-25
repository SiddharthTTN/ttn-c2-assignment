# Integrations

## PostgreSQL and PGVector

One durable PostgreSQL instance stores ticket records, comments, and vector chunks. Flyway owns schema and extension migrations. H2 may be used only for lightweight unit tests; PostgreSQL/Testcontainers is required for persistence, search, and vector integration tests.

## Embedding provider

Spring AI's `EmbeddingModel` abstraction uses Ollama `nomic-embed-text` with 768 dimensions by default or a configured cloud provider. Model name, base URL, credentials, and bounded HTTP connect/read timeouts (`app.ollama.http.*`, overridable via `OLLAMA_HTTP_*`) are runtime settings. The complete ask pipeline runs in a bounded executor and has one configurable 7.5-second deadline (`app.rag.ask-timeout`, overridable via `ASK_TIMEOUT`) across embedding, retrieval, and generation; expiry cancels the task and returns 503. Embedding responses must match `app.rag.embedding-dimensions`; mismatches are server errors, not model-outage 503s. A dimension change requires database migration and re-indexing.

## Chat provider

Spring AI's `ChatModel` uses Ollama `llama3.2:3b` by default and performs one constrained generation per qualifying ask. The prompt forbids outside knowledge and requests a structured answer with source ticket ids. No tools or agent loop are registered.

Failures are bounded by the total ask deadline and provider timeouts and exposed as 503 with a correlation id. Generation is not retried automatically; ingestion retries are safe because chunks are replaced by ticket/version.
