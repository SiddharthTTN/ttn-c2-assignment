# Glossary

- **Grounded answer:** response derived only from chunks retrieved for the current question.
- **Citation:** a ticket id from the qualifying retrieval set returned with an answer.
- **No-match:** fixed response used when no chunk meets the configured similarity cutoff.
- **Knowledge refresh:** replacement of all vector chunks and metadata for one ticket version.
- **Terminal status:** CLOSED or CANCELLED; RESOLVED may only advance to CLOSED and cannot reopen.
- **Top-K:** maximum vector chunks considered before thresholding and ticket grouping.
- **Similarity cutoff:** minimum configured relevance score for a chunk to qualify.
- **Steering artefact:** repository rule, skill, or command that constrains AI-assisted development.
