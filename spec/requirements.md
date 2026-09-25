# Requirements

The approved scope is an unauthenticated, single-workspace support-ticket application.

## Functional scope

- FR-1–FR-6: create, list, view, update, assign, and comment on durable tickets.
- FR-7–FR-8: case-insensitive title/description search and status filtering.
- FR-9–FR-10: enforce only OPEN → IN_PROGRESS → RESOLVED → CLOSED, OPEN → CANCELLED, and IN_PROGRESS → CANCELLED.
- FR-11–FR-12: survive restart and return meaningful validation errors.
- FR-13–FR-18: ingest and refresh ticket knowledge; retrieve then generate grounded, cited answers; return an explicit no-match; configure top-K and similarity cutoff.
- FR-19–FR-20: seed demonstrable ticket history and expose Q&A in the UI.
- FR-21–FR-25: supply steering artefacts, complete specs before code, retain prompt history, document one real AI mistake, and commit no secrets.

## Quality scope

- NFR-1: ticket operations p95 <500 ms for up to 1,000 tickets; ask p95 <8 seconds.
- NFR-2: no authentication for this exercise; runtime-only model credentials; answers may use only retrieved content.
- NFR-3: durable records, fail-closed transitions, and no fabricated no-match answers.
- NFR-4: structured transition and ask logs without chain-of-thought.
- NFR-5: JSON/HTTP APIs and desktop support for the latest two Chrome and latest Firefox releases.
- NFR-6: keyboard-operable, labeled controls with associated errors.

## Exclusions

Authentication, authorization, multi-tenancy, attachments, SLAs, customer portals, email/chat ingestion, autonomous tools, notifications, and reopening terminal tickets are out of scope.
