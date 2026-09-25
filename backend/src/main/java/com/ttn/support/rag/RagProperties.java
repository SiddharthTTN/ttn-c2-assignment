package com.ttn.support.rag;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.rag")
public class RagProperties {

    @Min(1)
    @Max(100)
    private int topK = 8;

    @Min(0)
    @Max(1)
    private double similarityThreshold = 0.75;

    @Min(1)
    @Max(4096)
    private int embeddingDimensions = 768;

    @NotNull
    private Duration askTimeout = Duration.ofMillis(7500);

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public int getEmbeddingDimensions() {
        return embeddingDimensions;
    }

    public void setEmbeddingDimensions(int embeddingDimensions) {
        this.embeddingDimensions = embeddingDimensions;
    }

    public Duration getAskTimeout() {
        return askTimeout;
    }

    public void setAskTimeout(Duration askTimeout) {
        if (askTimeout == null || askTimeout.isZero() || askTimeout.isNegative()) {
            throw new IllegalArgumentException("app.rag.ask-timeout must be positive");
        }
        this.askTimeout = askTimeout;
    }
}
