package com.fga.example.service;

import com.fga.example.fga.FgaCheck;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class DocumentService {
    private final OpenFgaClient fgaClient;

    public DocumentService(OpenFgaClient fgaClient) {
        this.fgaClient = fgaClient;
    }

    /**
     * Simple Fga bean available for use in preauthorize. TODOs:
     * - If userId is not specified, is it a good default to use the current principal name?
     * - multiple params with optional params (e.g., userId) could be unwieldy
     */
    @PreAuthorize("@openFga.check('#id', 'document', 'reader', 'user')")
    public String getDocumentWithSimpleFgaBean(@PathVariable String id) {
        return "You have access!";
    }

    /**
     * Fga custom annotation with a {@code @Before} pointcut available for use in preauthorize. TODOs:
     * - If userId is not specified, is it a good default to use the current principal name?
     * - Do we need more flexibility in the pointcut?
     * - would https://github.com/spring-projects/spring-security/issues/14480 make this implementation easier, and would it support SpEL for fields like object and userId
     */
    @FgaCheck(userType="user", relation="reader", objectType="document", object="#id")
    public String customAnnotation(@PathVariable String id) {
        return "You have access!";
    }

    /**
     * Demonstrates a simple example of using the injected fgaClient to write authorization data to FGA.
     */
    public String createDoc(@RequestBody String id, Principal principal) {
        ClientWriteRequest writeRequest =  new ClientWriteRequest()
                .writes(List.of(new ClientTupleKey()
                        .user(String.format("user:%s", principal.getName()))
                        .relation("reader")
                        ._object(String.format("document:%s", id))));

        try {
            fgaClient.write(writeRequest).get();
        } catch (InterruptedException | ExecutionException | FgaInvalidParameterException e) {
            throw new RuntimeException("Error writing to FGA", e);
        }

        return String.format("Created doc with ID %s\n", id);
    }
}
