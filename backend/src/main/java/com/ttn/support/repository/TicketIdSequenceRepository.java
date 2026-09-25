package com.ttn.support.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TicketIdSequenceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public long nextTicketNumber() {
        Number current = (Number) entityManager
                .createNativeQuery("SELECT next_val FROM ticket_id_sequence FOR UPDATE")
                .getSingleResult();
        long next = current.longValue();
        entityManager
                .createNativeQuery("UPDATE ticket_id_sequence SET next_val = next_val + 1")
                .executeUpdate();
        entityManager.flush();
        return next;
    }
}
