package com.ttn.support.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class KnowledgeRefreshPublisher {

    private final KnowledgeRefreshService knowledgeRefreshService;

    public KnowledgeRefreshPublisher(KnowledgeRefreshService knowledgeRefreshService) {
        this.knowledgeRefreshService = knowledgeRefreshService;
    }

    public void scheduleAfterCommit(String ticketId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            knowledgeRefreshService.refreshTicket(ticketId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    knowledgeRefreshService.refreshTicket(ticketId);
                } catch (Exception ignored) {
                    // Ticket remains PENDING; scheduler retries.
                }
            }
        });
    }
}
