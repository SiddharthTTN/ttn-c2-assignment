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

## 2026-09-25T10:16:00Z — development — attempt 3
**Ask:** Resolve the cached-detail, PostgreSQL migration/runtime, provider-boundary, refresh-lock, integration-test, specification, and UI polish findings.
**Did:** Applied only the flagged fixes, added targeted route, HTTP conflict, transaction-boundary, dimension, migration, and PGVector integration coverage, and reran the complete local suites.
**Artifacts:** `backend/`, `frontend/`, `Makefile`, `spec/api-contract.md`, `spec/integrations.md`, `spec/rag-ingestion.md`, `spec/test-strategy.md`, `docs/prompt-history.md`, `artifacts/05-development-summary-v1.md`

## 2026-09-25T10:45:05Z — development — attempt 1
**Ask:** Resolve the reviewer’s stale-refresh, retry timing, asynchronous dispatch, total ask deadline, production validation, optimistic-lock, dialog focus, and data-type findings.
**Did:** Applied only the requested implementation and specification corrections, added focused concurrency, deadline, persistence, performance, startup, and UI regressions, and reran all locally available validation.
**Artifacts:** `backend/`, `frontend/`, `.github/workflows/backend.yml`, `spec/data-model.md`, `spec/error-handling.md`, `spec/integrations.md`, `spec/rag-ingestion.md`, `spec/test-strategy.md`, `docs/prompt-history.md`, `artifacts/05-development-summary-v1.md`

## 2026-09-25T11:16:00Z — development — attempt 1
**Ask:** Fix PostgreSQL Hibernate TEXT mapping so PGVector CI can start, complete production-profile acceptance evidence, and pin the current PR commit.
**Did:** Mapped schema text columns as LONGVARCHAR instead of @Lob, aligned H2, recorded a passing Docker PGVector CI run, and updated the complete development summary.
**Artifacts:** `backend/src/main/java/com/ttn/support/domain/`, `backend/src/main/resources/db/migration/h2/V1__schema.sql`, `spec/data-model.md`, `spec/test-strategy.md`, `docs/prompt-history.md`, `artifacts/05-development-summary-v1.md`

## 2026-09-25T11:22:18Z — development — attempt 1
**Ask:** Record successful production-profile PGVector acceptance, retain explicit live-model and latency boundaries, and pin the current delivery commit.
**Did:** Verified green Docker-enabled PGVector CI evidence, preserved the remaining Ollama and production-latency caveats, and refreshed the complete development summary.
**Artifacts:** `spec/test-strategy.md`, `docs/prompt-history.md`, `artifacts/05-development-summary-v1.md`

## 2026-09-25T11:26:38Z — testing — attempt 1
**Ask:** Check CI for PRs #1 and #2, fix any failures, and produce the complete v1 test report.
**Did:** Confirmed PR #2 CI green, recorded that PR #1 has no configured checks, reran backend and frontend validation, and documented test and coverage evidence.
**Artifacts:** `docs/prompt-history.md`, `artifacts/06-test-results-v1.md`

## 2026-09-25T11:30:56Z — testing — attempt 2
**Ask:** Eliminate skipped acceptance results and add measured coverage evidence meeting the default 70% threshold.
**Did:** Added JaCoCo and Vitest coverage gates, expanded backend branch tests, ran zero-skip local and Docker CI suites, and revised the complete test report.
**Artifacts:** `.github/workflows/backend.yml`, `backend/pom.xml`, `backend/src/test/`, `frontend/package.json`, `frontend/vite.config.ts`, `frontend/eslint.config.js`, `spec/test-strategy.md`, `docs/prompt-history.md`, `artifacts/06-test-results-v1.md`
