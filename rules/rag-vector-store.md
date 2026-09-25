# RAG & vector store rules

Authoritative specs: **`spec/rag-ingestion.md`**, **`spec/rag-api-contract.md`**, **`spec/architecture.md`**, **`spec/integrations.md`**, **`spec/data-model.md`**.

## Storage

- **Production**: PostgreSQL with **pgvector** extension; single database for relational tickets and vector chunks.
- **Demo/test**: H2 with deterministic embedding/retrieval substitutes — not for production vector quality validation.

## Embedding defaults

| Setting | Default | Notes |
|---------|---------|-------|
| Model | `nomic-embed-text` (Ollama) | Override via `OLLAMA_EMBED_MODEL` |
| Dimensions | **768** | `app.rag.embedding-dimensions` — change requires migration + re-embed |
| Chat model | `llama3.2:3b` | Override via `OLLAMA_CHAT_MODEL` |

## Retrieval defaults (runtime-overridable, startup-validated)

- `app.rag.top-k=8`
- `app.rag.similarity-threshold=0.75`

Ask flow (`POST /api/ai/ask`):

1. Embed question.
2. Retrieve chunks above similarity cutoff, respecting top-K.
3. Exclude stale/`PENDING` knowledge per ticket version rules.
4. Build context-only prompt; validate cited ticket ids ⊆ retrieved set.
5. Return structured JSON or no-match without hallucinated ids.

No-match constant answer text: `"no relevant tickets found"` (see `spec/rag-api-contract.md`).

## Ingestion & freshness

- On ticket create/update/comment/accepted status change: increment knowledge version, then **replace** all chunks for that ticket after DB commit.
- Failure → ticket knowledge state `PENDING`; scheduled bounded retry (`app.knowledge-retry.*`).
- Chunks split by semantic boundaries: description, each comment, resolution notes; large text uses ~500-token windows, 50-token overlap (`ChunkingService`).

## Chunk metadata (required)

Each stored chunk must carry at minimum: ticket id, status, priority, assignee, category, source type, source id, content hash, ticket/knowledge version (see data model vector section).

## Operations

```bash
make docker-up      # PostgreSQL pgvector + Ollama
make ollama-pull    # default models
make run-postgres   # backend with postgres profile
```

Environment variables: `.env.example` (`DATABASE_*`, `OLLAMA_*`).

## Safety & quality

- Prompt must forbid outside knowledge; unsupported citations fail closed as no-match.
- Provider timeout/outage → **503** with standard error body; no silent fallback to ungrounded answers.
- Do not log full ticket bodies or embeddings at INFO in production paths.

## Testing & evaluation

- Unit: chunking, citation filtering, threshold behavior.
- Integration: ask endpoint with deterministic fakes in `test` profile.
- Evaluation corpus: `spec/evaluation-strategy.md`.

## Agent review focus (`commands/review-rag-output.md`)

When reviewing RAG changes, verify version bump on every mutating path, atomic chunk replacement, stale exclusion at query time, and ask JSON contract — not just “a vector column exists.”
