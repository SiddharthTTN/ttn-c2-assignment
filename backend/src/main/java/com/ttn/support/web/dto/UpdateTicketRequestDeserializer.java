package com.ttn.support.web.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ttn.support.domain.TicketPriority;
import java.io.IOException;

public class UpdateTicketRequestDeserializer extends JsonDeserializer<UpdateTicketRequest> {

    @Override
    public UpdateTicketRequest deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (!node.isObject()) {
            throw new IllegalArgumentException("PATCH body must be a JSON object");
        }
        ObjectNode object = (ObjectNode) node;
        if (object.isEmpty()) {
            throw new IllegalArgumentException("At least one field must be provided");
        }

        UpdateTicketRequest request = new UpdateTicketRequest();
        applyField(object, "title", request::setTitlePresent, value -> request.setTitle(value.asText()));
        applyField(object, "description", request::setDescriptionPresent, value -> request.setDescription(value.asText()));
        applyPriorityField(object, request);
        applyNullableField(object, "assignee", request::setAssigneePresent, value -> request.setAssignee(value.asText()));
        applyNullableField(object, "category", request::setCategoryPresent, value -> request.setCategory(value.asText()));
        applyNullableField(
                object,
                "resolutionNotes",
                request::setResolutionNotesPresent,
                value -> request.setResolutionNotes(value.asText()));
        return request;
    }

    private void applyField(
            ObjectNode object,
            String fieldName,
            java.util.function.Consumer<Boolean> presenceSetter,
            java.util.function.Consumer<JsonNode> valueSetter) {
        if (!object.has(fieldName)) {
            return;
        }
        presenceSetter.accept(true);
        JsonNode value = object.get(fieldName);
        if (value.isNull()) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        valueSetter.accept(value);
    }

    private void applyNullableField(
            ObjectNode object,
            String fieldName,
            java.util.function.Consumer<Boolean> presenceSetter,
            java.util.function.Consumer<JsonNode> valueSetter) {
        if (!object.has(fieldName)) {
            return;
        }
        presenceSetter.accept(true);
        JsonNode value = object.get(fieldName);
        if (!value.isNull()) {
            valueSetter.accept(value);
        }
    }

    private void applyPriorityField(ObjectNode object, UpdateTicketRequest request) {
        if (!object.has("priority")) {
            return;
        }
        request.setPriorityPresent(true);
        JsonNode value = object.get("priority");
        if (value.isNull()) {
            throw new IllegalArgumentException("priority cannot be null");
        }
        request.setPriority(TicketPriority.from(value.asText()));
    }
}
