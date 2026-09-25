package com.ttn.support.repository;

import com.ttn.support.domain.Ticket;
import com.ttn.support.domain.TicketStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class TicketSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<Ticket> search(TicketStatus status, String pattern) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ticket t WHERE 1=1");
        if (status != null) {
            sql.append(" AND t.status = :status");
        }
        if (pattern != null) {
            sql.append(" AND (LOWER(CAST(t.title AS VARCHAR)) LIKE LOWER(:pattern) ESCAPE '!'");
            sql.append(" OR LOWER(CAST(t.description AS VARCHAR)) LIKE LOWER(:pattern) ESCAPE '!')");
        }
        sql.append(" ORDER BY t.updated_at DESC");
        var query = entityManager.createNativeQuery(sql.toString(), Ticket.class);
        if (status != null) {
            query.setParameter("status", status.name());
        }
        if (pattern != null) {
            query.setParameter("pattern", pattern);
        }
        return query.getResultList();
    }
}
