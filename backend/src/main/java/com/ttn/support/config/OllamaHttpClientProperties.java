package com.ttn.support.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.ollama.http")
public class OllamaHttpClientProperties {

    /**
     * Upper bound for Ollama read timeout per integration design (7.5 seconds).
     */
    public static final Duration MAX_READ_TIMEOUT = Duration.ofMillis(7500);

    private Duration connectTimeout = Duration.ofSeconds(2);

    private Duration readTimeout = Duration.ofMillis(7500);

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        if (readTimeout.compareTo(MAX_READ_TIMEOUT) > 0) {
            throw new IllegalArgumentException(
                    "app.ollama.http.read-timeout must be at most " + MAX_READ_TIMEOUT.toMillis() + "ms");
        }
        this.readTimeout = readTimeout;
    }
}
