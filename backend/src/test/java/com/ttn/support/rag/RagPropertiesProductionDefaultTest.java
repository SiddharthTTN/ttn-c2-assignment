package com.ttn.support.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

class RagPropertiesProductionDefaultTest {

    @Test
    void productionProfileKeepsDefaultSimilarityThreshold() throws Exception {
        @SuppressWarnings("unchecked")
        var root = (java.util.Map<String, Object>) new Yaml()
                .load(Files.readString(Path.of("src/main/resources/application.yml")));
        @SuppressWarnings("unchecked")
        var app = (java.util.Map<String, Object>) root.get("app");
        @SuppressWarnings("unchecked")
        var rag = (java.util.Map<String, Object>) app.get("rag");
        assertEquals(0.75, ((Number) rag.get("similarity-threshold")).doubleValue());
    }
}
