package com.ttn.support.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.profiles.active=test")
class TicketIdGeneratorTest {

    @Autowired
    private TicketIdGenerator ticketIdGenerator;

    @Test
    @Transactional
    void formatsSequenceIds() {
        String id = ticketIdGenerator.nextId();
        assertTrue(id.matches("TKT-\\d+"));
    }
}
