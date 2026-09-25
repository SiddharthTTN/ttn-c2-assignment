package com.ttn.support.db;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ttn.support.domain.Ticket;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.RollbackException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.profiles.active=test")
class TicketJpaOptimisticLockTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void concurrentJpaUpdatesProduceVersionCollision() {
        EntityManager first = entityManagerFactory.createEntityManager();
        EntityManager second = entityManagerFactory.createEntityManager();
        try {
            first.getTransaction().begin();
            second.getTransaction().begin();
            Ticket firstCopy = first.find(Ticket.class, "TKT-1001");
            Ticket staleCopy = second.find(Ticket.class, "TKT-1001");

            firstCopy.setTitle("first writer");
            staleCopy.setTitle("stale writer");
            first.getTransaction().commit();

            assertThrows(RollbackException.class, () -> second.getTransaction().commit());
        } finally {
            if (first.getTransaction().isActive()) {
                first.getTransaction().rollback();
            }
            if (second.getTransaction().isActive()) {
                second.getTransaction().rollback();
            }
            first.close();
            second.close();
        }
    }
}
