# RAG evaluation strategy

Maintain a versioned seed corpus and question set for payment failures, TKT-1001 resolution, shipment tracking causes, similar resolved tickets, high-priority payment tickets, and unrelated questions.

Automated checks assert:

- expected ticket recall within configured top-K;
- no retrieved chunk below the configured cutoff;
- every citation belongs to the retrieval set;
- no-match uses the exact fixed response and invokes no chat model;
- updates and comments replace stale knowledge before subsequent answers;
- generated claims are textually supported by retrieved ticket content.

Record model/provider/profile, top-K, cutoff, latency, retrieval ids, citations, and pass/fail without storing chain-of-thought. A human hallucination review uses the repository command and records at least one real caught mistake during development.
