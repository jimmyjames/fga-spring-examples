package com.fga.example.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import dev.openfga.sdk.api.client.model.ClientRelationshipCondition;
import dev.openfga.sdk.api.client.model.ClientTupleKey;

import java.io.IOException;

public class OpenFgaClientTupleKeyDeserializer extends JsonDeserializer<ClientTupleKey> {
    @Override
    public ClientTupleKey deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        ClientTupleKey key = new ClientTupleKey();
        if (node.has("user")) key.user(node.get("user").asText());
        if (node.has("relation")) key.relation(node.get("relation").asText());
        if (node.has("condition")) key.condition(makeCondition(node.get("condition")));
        if (node.has("_object")) {
            key._object(node.get("_object").asText());
        } else if (node.has("object")) {
            key._object(node.get("object").asText());
        }
        return key;
    }

    private ClientRelationshipCondition makeCondition(JsonNode node) {
        ClientRelationshipCondition condition = new ClientRelationshipCondition();
        if (node.has("name")) condition.name(node.get("name").asText());
        if (node.has("context")) condition.context(node.get("context"));
        return condition;
    }
}
