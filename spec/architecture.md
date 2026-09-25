# Architecture

Use a single repository with a Java 21/Spring Boot backend, a React/TypeScript frontend, PostgreSQL with PGVector, and Spring AI adapters for embedding and chat models. The browser calls only the backend JSON API. The backend owns validation, lifecycle rules, persistence, retrieval, citations, and no-match decisions.

Ticket writes run in a database transaction and synchronously replace that ticket's vector documents after commit. A failed refresh marks the ticket knowledge state `PENDING` and is retried by a bounded scheduled worker; asks exclude stale/pending documents. This keeps source records durable while preventing stale answers.

Ticket knowledge is split by semantic field boundaries: description, each comment, and resolution notes. Oversized fields use paragraph-aware windows of about 500 tokens with 50-token overlap. Each chunk carries ticket id, status, priority, assignee, category, source type, source id, content hash, and version.

PGVector is selected to keep relational and vector state in one durable service. The default Ollama profile uses `nomic-embed-text` with 768 dimensions and `llama3.2:3b`. This minimizes data egress and cost but increases local latency and setup; a compatible cloud provider may improve quality/latency but requires credentials and transmits retrieved text. Model names remain runtime-overridable, but embedding-dimension changes require migration and re-indexing.

Retrieval properties default to `app.rag.top-k=8` and `app.rag.similarity-threshold=0.75`; both are runtime-overridable and startup-validated. The ask flow embeds the question, applies similarity cutoff, groups qualifying chunks by ticket, builds a context-only prompt, and validates model-cited ticket ids against the retrieved set before returning.
