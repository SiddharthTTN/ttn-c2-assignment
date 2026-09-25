# Prompt history

## 2026-09-25T07:47:00Z — design — attempt 1
**Ask:** Produce the v1 technical design for the grounded support-ticket application and required repository hygiene.
**Did:** Established the living specification, designed the complete architecture, data, API, RAG, UI, security, error, and observability contracts, and prepared the review artifact.
**Artifacts:** `spec/README.md`, `spec/requirements.md`, `spec/architecture.md`, `spec/data-model.md`, `spec/api-contract.md`, `spec/state-machine.md`, `spec/ui-flow.md`, `spec/error-handling.md`, `spec/security.md`, `spec/integrations.md`, `spec/test-strategy.md`, `spec/glossary.md`, `spec/rag-ingestion.md`, `spec/rag-api-contract.md`, `spec/evaluation-strategy.md`, `docs/prompt-history.md`, `artifacts/03-technical-design-v1.md`

## 2026-09-25T07:55:00Z — design — attempt 2
**Ask:** Revise the v1 technical design to resolve the reviewer’s search, API-contract, RAG-default, UI, spec-linkage, and observability findings.
**Did:** Applied only the requested corrections, aligned affected living specs, and preserved all unflagged design sections and behavior.
**Artifacts:** `artifacts/03-technical-design-v1.md`, `spec/architecture.md`, `spec/data-model.md`, `spec/api-contract.md`, `spec/integrations.md`, `spec/ui-flow.md`, `docs/prompt-history.md`

## 2026-09-25T08:35:00Z — development — attempt 1
**Ask:** Implement, validate, and deliver the approved grounded support-ticket backend, frontend, and repository hygiene artefacts.
**Did:** Built the Spring Boot API and RAG pipeline, polished React support console, persistence and seed data, contract and UI tests, Swagger, local/production workflows, steering files, and documented real AI mistakes caught during review.
**Artifacts:** `backend/`, `frontend/`, `rules/`, `skills/documentation/`, `commands/`, `scripts/`, `README.md`, `Makefile`, `docker-compose.yml`, `.env.example`, `docs/ai-mistakes.md`, `spec/data-model.md`, `docs/prompt-history.md`, `.specstory/history/2026-09-25-repository-hygiene.md`, `artifacts/05-development-summary-v1.md`

## 2026-09-25T09:22:00Z — development — attempt 2
**Ask:** Revise the implementation and complete development summary to resolve the reviewer’s routing, vector mapping, concurrency, grounding, conflict, testing, error, retry, and validation findings.
**Did:** Corrected only the flagged backend and frontend paths, added focused regression coverage, ran the full local validation suites, and made production-profile validation limits explicit.
**Artifacts:** `backend/`, `frontend/`, `spec/data-model.md`, `spec/error-handling.md`, `spec/test-strategy.md`, `docs/prompt-history.md`, `artifacts/05-development-summary-v1.md`
