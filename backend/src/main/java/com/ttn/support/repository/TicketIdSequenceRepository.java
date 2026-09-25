package com.ttn.support.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class TicketIdSequenceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public long nextTicketNumber() {
        Number current = (Number) entityManager
                .createNativeQuery("SELECT next_val FROM ticket_id_sequence")
                .getSingleResult();
        long next = current.longValue();
        entityManager
                .createNativeQuery("UPDATE ticket_id_sequence SET next_val = next_val + 1")
                .executeUpdate();
        return next;
    }
}
