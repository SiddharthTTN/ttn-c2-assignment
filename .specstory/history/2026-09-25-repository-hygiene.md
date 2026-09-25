## 2026-09-25T08:49:00Z — repository hygiene & delivery artefacts

**Ask:** Add root developer-experience and hygiene files without modifying `backend/`, `frontend/`, or `spec/` sources: agent rules, commands, documentation skill, `.env.example`, `.gitignore`, `README.md`, `docker-compose.yml`, root `Makefile`/scripts for install/test/build/run, production packaging of frontend `dist` into the backend JAR via staged static resources (gitignored), and `docs/ai-mistakes.md` for URGENT priority, comment response type, and PATCH null semantics. Record this prompt in history. No commit/push/branch switch.

**Did:** Created `rules/`, `commands/`, `skills/documentation/`, `scripts/`, root Makefile, compose stack, env template, and AI mistake postmortem aligned with living specs and existing contract tests.

**Artifacts:** `README.md`, `Makefile`, `docker-compose.yml`, `.env.example`, `.gitignore`, `scripts/copy-frontend-static.sh`, `scripts/clean-frontend-static.sh`, `rules/*.md`, `commands/*.md`, `skills/documentation/SKILL.md`, `docs/ai-mistakes.md`, `.specstory/history/2026-09-25-repository-hygiene.md`
