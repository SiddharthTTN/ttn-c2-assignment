package com.ttn.support.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SearchPatternBuilderTest {

    private final SearchPatternBuilder builder = new SearchPatternBuilder();

    @Test
    void escapesWildcardCharacters() {
        assertEquals("%50!% off%", builder.toLikePattern("50% off"));
        assertEquals("%a!_b%", builder.toLikePattern("a_b"));
    }

    @Test
    void blankQueryReturnsNullPattern() {
        assertNull(builder.toLikePattern(" "));
    }
}
