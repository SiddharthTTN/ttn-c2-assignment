package com.ttn.support.service;

import com.ttn.support.rag.KnowledgeRetrievalService;
import com.ttn.support.rag.RagProperties;
import com.ttn.support.rag.RetrievedChunk;
import com.ttn.support.rag.TicketAnswerGenerator;
import com.ttn.support.rag.TicketEmbeddingService;
import com.ttn.support.web.dto.AskRequest;
import com.ttn.support.web.dto.AskResponse;
import com.ttn.support.web.error.ModelUnavailableException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AskService {

    public static final String NO_MATCH_ANSWER = "no relevant tickets found";

    private static final Logger log = LoggerFactory.getLogger("com.ttn.support.ask");

    private final TicketEmbeddingService embeddingService;
    private final KnowledgeRetrievalService retrievalService;
    private final TicketAnswerGenerator answerGenerator;
    private final RagProperties ragProperties;
    private final ExecutorService askExecutor;

    public AskService(
            TicketEmbeddingService embeddingService,
            KnowledgeRetrievalService retrievalService,
            TicketAnswerGenerator answerGenerator,
            RagProperties ragProperties,
            @Qualifier("askExecutor") ExecutorService askExecutor) {
        this.embeddingService = embeddingService;
        this.retrievalService = retrievalService;
        this.answerGenerator = answerGenerator;
        this.ragProperties = ragProperties;
        this.askExecutor = askExecutor;
    }

    public AskResponse ask(AskRequest request) {
        Future<AskResponse> ask = null;
        try {
            ask = askExecutor.submit(() -> executeAsk(request));
            return ask.get(ragProperties.getAskTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            ask.cancel(true);
            throw new ModelUnavailableException("Ask deadline exceeded", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            if (ask != null) {
                ask.cancel(true);
            }
            throw new ModelUnavailableException("Ask interrupted", ex);
        } catch (RejectedExecutionException ex) {
            throw new ModelUnavailableException("Ask capacity exhausted", ex);
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Ask execution failed", ex.getCause());
        }
    }

    private AskResponse executeAsk(AskRequest request) {
        String question = request.getQuestion();
        float[] queryEmbedding = embeddingService.embed(question);
        List<RetrievedChunk> retrieved = retrievalService.retrieve(
                queryEmbedding, question, ragProperties.getTopK(), ragProperties.getSimilarityThreshold());

        if (retrieved.isEmpty()) {
            log.info(
                    "askOutcome=NO_MATCH topK={} threshold={} retrievedCount=0 citations=[]",
                    ragProperties.getTopK(),
                    ragProperties.getSimilarityThreshold());
            return new AskResponse(NO_MATCH_ANSWER, List.of(), true);
        }

        Set<String> allowedTicketIds = retrieved.stream()
                .map(RetrievedChunk::ticketId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        TicketAnswerGenerator.GeneratedAnswer generated =
                answerGenerator.generate(question, retrieved, allowedTicketIds);

        List<String> citations = validateCitations(generated.ticketIds(), allowedTicketIds);
        if (citations.isEmpty() || NO_MATCH_ANSWER.equalsIgnoreCase(generated.answer())) {
            log.info(
                    "askOutcome=NO_MATCH topK={} threshold={} retrievedCount={} citations=[]",
                    ragProperties.getTopK(),
                    ragProperties.getSimilarityThreshold(),
                    retrieved.size());
            return new AskResponse(NO_MATCH_ANSWER, List.of(), true);
        }

        log.info(
                "askOutcome=ANSWER topK={} threshold={} retrievedCount={} citations={}",
                ragProperties.getTopK(),
                ragProperties.getSimilarityThreshold(),
                retrieved.size(),
                citations);
        return new AskResponse(generated.answer(), citations, false);
    }

    private List<String> validateCitations(List<String> proposed, Set<String> allowed) {
        List<String> valid = new ArrayList<>();
        for (String ticketId : proposed) {
            if (allowed.contains(ticketId) && !valid.contains(ticketId)) {
                valid.add(ticketId);
            }
        }
        return valid;
    }
}
