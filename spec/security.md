# Security

Authentication and authorization are intentionally absent for this single-workspace exercise. The deployment must therefore bind to a trusted local/internal network and must not be exposed publicly unchanged.

Bean validation, enum parsing, bounded field lengths, parameterized JPA queries, and escaped React rendering protect input boundaries. Model credentials come only from environment variables or external secret stores and never from tracked configuration. Logs omit raw prompts, ticket bodies, model responses, credentials, and chain-of-thought.

The grounded-answer boundary supplies only retrieved chunks to the model. The server intersects all returned citations with retrieved ticket ids and rejects unsupported citations; no qualifying retrieval returns the fixed no-match response without invoking generation.
