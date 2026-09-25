package com.ttn.support.repository;

import com.ttn.support.domain.KnowledgeState;
import com.ttn.support.domain.Ticket;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.id = :ticketId")
    Optional<Ticket> findByIdForKnowledgeRefresh(@Param("ticketId") String ticketId);

    @Query("""
            SELECT t FROM Ticket t
            WHERE t.knowledgeState = :state
              AND (t.knowledgeNextRetryAt IS NULL OR t.knowledgeNextRetryAt <= :now)
            """)
    List<Ticket> findPendingKnowledgeRefreshDue(
            @Param("state") KnowledgeState state, @Param("now") Instant now);
}
