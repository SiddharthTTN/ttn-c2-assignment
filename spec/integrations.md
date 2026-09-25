# Integrations

## PostgreSQL and PGVector

One durable PostgreSQL instance stores ticket records, comments, and vector chunks. Flyway owns schema and extension migrations. H2 may be used only for lightweight unit tests; PostgreSQL/Testcontainers is required for persistence, search, and vector integration tests.

## Embedding provider

Spring AI's `EmbeddingModel` abstraction selects Ollama by default for local development or a configured cloud provider. Model name, base URL, credentials, dimensions, and request timeout are runtime settings.

## Chat provider

Spring AI's `ChatModel` performs one constrained generation per qualifying ask. The prompt forbids outside knowledge and requests a structured answer with source ticket ids. No tools or agent loop are registered.

Failures are bounded by provider timeouts and exposed as 503 with a correlation id. Generation is not retried automatically; ingestion retries are safe because chunks are replaced by ticket/version.
