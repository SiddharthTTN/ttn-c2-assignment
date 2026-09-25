package com.ttn.support.service;

import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class KnowledgeRefreshPublisher {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRefreshPublisher.class);

    private final KnowledgeRefreshService knowledgeRefreshService;
    private final TaskExecutor knowledgeRefreshExecutor;

    public KnowledgeRefreshPublisher(
            KnowledgeRefreshService knowledgeRefreshService,
            @Qualifier("knowledgeRefreshExecutor") TaskExecutor knowledgeRefreshExecutor) {
        this.knowledgeRefreshService = knowledgeRefreshService;
        this.knowledgeRefreshExecutor = knowledgeRefreshExecutor;
    }

    public void scheduleAfterCommit(String ticketId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            submit(ticketId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                submit(ticketId);
            }
        });
    }

    private void submit(String ticketId) {
        try {
            knowledgeRefreshExecutor.execute(() -> {
                try {
                    knowledgeRefreshService.refreshTicket(ticketId);
                } catch (Exception ex) {
                    // Ticket remains PENDING; scheduler retries.
                    log.debug("Asynchronous knowledge refresh failed ticketId={}", ticketId);
                }
            });
        } catch (RejectedExecutionException ex) {
            // Ticket remains PENDING; scheduler retries.
            log.warn("Knowledge refresh queue full ticketId={}", ticketId);
        }
    }
}
