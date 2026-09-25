package com.ttn.support.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class OllamaHttpClientPropertiesContractTest {

    @Test
    void postgresProfileDocumentsBoundedOllamaHttpTimeoutsWithOverrides() throws Exception {
        String yaml = Files.readString(Path.of("src/main/resources/application-postgres.yml"));

        assertTrue(yaml.contains("connect-timeout:"), "connect timeout must be configurable");
        assertTrue(yaml.contains("OLLAMA_HTTP_CONNECT_TIMEOUT"), "connect timeout must allow runtime override");
        assertTrue(yaml.contains("read-timeout:"), "read timeout must be configurable");
        assertTrue(yaml.contains("OLLAMA_HTTP_READ_TIMEOUT"), "read timeout must allow runtime override");
        assertTrue(yaml.contains("7s"), "default read timeout must stay within the 7.5s design budget");
    }

    @Test
    void readTimeoutCannotExceedDesignBudget() {
        OllamaHttpClientProperties properties = new OllamaHttpClientProperties();
        properties.setReadTimeout(Duration.ofMillis(7500));
        assertEquals(Duration.ofMillis(7500), properties.getReadTimeout());

        assertThrows(IllegalArgumentException.class, () -> properties.setReadTimeout(Duration.ofMillis(7501)));
    }
}
