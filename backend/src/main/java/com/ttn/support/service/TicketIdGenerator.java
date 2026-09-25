package com.ttn.support.service;

import com.ttn.support.repository.TicketIdSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketIdGenerator {

    private final TicketIdSequenceRepository sequenceRepository;

    public TicketIdGenerator(TicketIdSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    @Transactional
    public String nextId() {
        return "TKT-" + sequenceRepository.nextTicketNumber();
    }
}
