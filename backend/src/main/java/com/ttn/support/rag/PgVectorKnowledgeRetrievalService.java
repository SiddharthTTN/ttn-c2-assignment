package com.ttn.support.rag;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("postgres")
public class PgVectorKnowledgeRetrievalService implements KnowledgeRetrievalService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<RetrievedChunk> retrieve(
            float[] queryEmbedding, String queryText, int topK, double similarityThreshold) {
        String vectorLiteral = EmbeddingCodec.toPgVectorLiteral(EmbeddingCodec.normalize(queryEmbedding));
        double maxDistance = 1.0 - similarityThreshold;
        List<Object[]> rows = entityManager
                .createNativeQuery(
                        """
                        SELECT k.ticket_id, k.content, k.ticket_version,
                               (1 - (k.embedding <=> CAST(:query AS vector))) AS similarity
                        FROM ticket_knowledge k
                        JOIN ticket t ON t.id = k.ticket_id
                        WHERE t.knowledge_state = 'READY'
                          AND k.ticket_version = t.knowledge_version
                          AND (k.embedding <=> CAST(:query AS vector)) <= :maxDistance
                        ORDER BY k.embedding <=> CAST(:query AS vector)
                        LIMIT :topK
                        """)
                .setParameter("query", vectorLiteral)
                .setParameter("maxDistance", maxDistance)
                .setParameter("topK", topK)
                .getResultList();

        List<RetrievedChunk> chunks = new ArrayList<>();
        for (Object[] row : rows) {
            chunks.add(new RetrievedChunk(
                    (String) row[0],
                    (String) row[1],
                    ((Number) row[3]).doubleValue(),
                    ((Number) row[2]).longValue()));
        }
        return chunks;
    }
}
