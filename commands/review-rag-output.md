# Review RAG output

Evaluate retrieval, ingestion, and `POST /api/ai/ask` responses for grounding, freshness, and contract compliance.

## References

- `spec/rag-api-contract.md` — response JSON, no-match text, 503 behavior
- `spec/rag-ingestion.md` — chunk boundaries, version replace, PENDING/retry
- `spec/evaluation-strategy.md` — seed questions and expected ticket ids
- `rules/rag-vector-store.md`

## Ingestion review

For each ticket mutation path (create, PATCH, comment, status):

- [ ] Knowledge version incremented
- [ ] Chunk set fully replaced after commit (no partial orphan chunks)
- [ ] Metadata on chunks complete (status, priority, assignee, category, source type/id, hash, version)
- [ ] Failure marks ticket knowledge `PENDING` and excludes stale chunks from retrieval until success

## Retrieval review

- [ ] `top-k` and `similarity-threshold` respected
- [ ] Stale/`PENDING` tickets excluded
- [ ] Similarity uses normalized embeddings (768-dim profile)

## Ask response review

Inspect JSON:

```json
{
  "answer": "...",
  "ticketIds": ["TKT-1001"],
  "noRelevantTickets": false
}
```

Checklist:

- [ ] `ticketIds` ⊆ tickets represented in retrieved chunks
- [ ] No outside knowledge when chunks insufficient — must return no-match shape
- [ ] No-match uses exact answer string from spec when applicable
- [ ] 503 on provider failure with standard error body (no partial answer)

## Manual probe (postgres profile)

```bash
make docker-up && make ollama-pull
make run-postgres
curl -s -X POST http://localhost:8080/api/ai/ask \
  -H 'Content-Type: application/json' \
  -d '{"question":"What caused previous payment failures?"}' | jq .
```

Compare against evaluation seed expectations.

## Output

Report:

1. **Grounding** — citations supported by retrieved text?
2. **Freshness** — after ticket edit, old content absent from answers?
3. **Contract** — JSON fields and HTTP codes?
4. **Failures** — actionable code/spec fixes with test suggestions (`AskApiIntegrationTest`, evaluation fixtures)
