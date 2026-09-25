package com.ttn.support.rag;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TicketIdQuestionParser {

    private static final Pattern TICKET_ID = Pattern.compile("TKT-\\d+", Pattern.CASE_INSENSITIVE);

    private TicketIdQuestionParser() {}

    public static List<String> extractTicketIds(String question) {
        if (question == null || question.isBlank()) {
            return List.of();
        }
        Set<String> ids = new LinkedHashSet<>();
        Matcher matcher = TICKET_ID.matcher(question);
        while (matcher.find()) {
            ids.add(normalize(matcher.group()));
        }
        return List.copyOf(ids);
    }

    public static String normalize(String ticketId) {
        String trimmed = ticketId.trim().toUpperCase(Locale.ROOT);
        if (!trimmed.startsWith("TKT-")) {
            return trimmed;
        }
        return trimmed;
    }
}
