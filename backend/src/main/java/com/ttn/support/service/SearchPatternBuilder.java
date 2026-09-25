package com.ttn.support.service;

import org.springframework.stereotype.Component;

@Component
public class SearchPatternBuilder {

    public String toLikePattern(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String escaped = escapeLike(query.trim());
        return "%" + escaped + "%";
    }

    static String escapeLike(String value) {
        StringBuilder out = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '!' || c == '%' || c == '_') {
                out.append('!');
            }
            out.append(c);
        }
        return out.toString();
    }
}
