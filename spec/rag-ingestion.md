# RAG ingestion

Each ticket version produces separate documents for description, each comment, and nonblank resolution notes. Field boundaries are preserved. Text over roughly 500 model tokens is paragraph-split and then windowed with roughly 50-token overlap; tiny adjacent paragraphs from the same source may be combined.

Every chunk stores ticket id, status, priority, assignee, category, source type/id, index, content hash, and ticket version. Create, field update, comment, and accepted status change increment `knowledge_version`. After transaction commit, the ingester embeds a complete new chunk set and atomically replaces the prior set. Until replacement succeeds, the ticket is `PENDING` and its old chunks are ineligible for retrieval. A scheduled bounded retry heals transient failures.
