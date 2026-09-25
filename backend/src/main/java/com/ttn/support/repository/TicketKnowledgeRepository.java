package com.ttn.support.repository;

import com.ttn.support.domain.TicketKnowledge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketKnowledgeRepository extends JpaRepository<TicketKnowledge, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TicketKnowledge k WHERE k.ticketId = :ticketId")
    void deleteByTicketId(@Param("ticketId") String ticketId);

    List<TicketKnowledge> findByTicketId(String ticketId);

    @Query("""
            SELECT k FROM TicketKnowledge k, Ticket t
            WHERE k.ticketId = t.id
              AND t.knowledgeState = com.ttn.support.domain.KnowledgeState.READY
              AND k.ticketVersion = t.knowledgeVersion
            """)
    List<TicketKnowledge> findAllEligible();

    @Query("""
            SELECT k FROM TicketKnowledge k, Ticket t
            WHERE k.ticketId = t.id
              AND k.ticketId IN :ticketIds
              AND t.knowledgeState = com.ttn.support.domain.KnowledgeState.READY
              AND k.ticketVersion = t.knowledgeVersion
            """)
    List<TicketKnowledge> findEligibleByTicketIds(@Param("ticketIds") List<String> ticketIds);
}
