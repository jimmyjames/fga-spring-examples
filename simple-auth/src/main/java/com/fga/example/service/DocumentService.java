package com.fga.example.service;

import com.fga.example.fga.*;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class DocumentService {

    private final OpenFgaClient fgaClient;

    public DocumentService(OpenFgaClient fgaClient) {
        this.fgaClient = fgaClient;
    }

    /**
     * Simple Fga bean available for use in preauthorize.
     * - multiple params of same type with optional params (e.g., userId) could be unwieldy
     */
    @PreAuthorize("@openFga.check(#id, 'document', 'reader', 'user', authentication?.name)")
    public Document getDocumentWithPreAuthorize(String id) {
            return new Document(id, "You have reader access to this document");
    }

    /**
     * Uses the new PreAuthorize meta-annotation support
     */
    @PreOpenFgaCheck(userType="'user'", relation="'reader'", objectType="'document'", object="#id")
    public Document getDocumentWithPreOpenFgaCheck(String id) {
        return new Document(id, "You have reader access to this document");
    }

    /**
     * Uses the new PreAuthorize meta-annotation support
     */
    @PreReadDocumentCheck("#id")
    public Document getDocumentWithPreReadDocumentCheck(String id) {
        return new Document(id, "You have reader access to this document");
    }

    @PostOpenFgaCheck(userType="'user'", relation="'reader'", objectType="'document'", object="returnObject.id")
    public Document findByContentWithPostOpenFgaCheck(String content) {
        return new Document("1", "Found the content here: '" + content + "'");
    }

    @PostReadDocumentCheck("returnObject.id")
    public Document findByContentWithPostReadDocumentCheck(String content) {
        return new Document("1", "Found the content here: '" + content + "'");
    }

    /**
     * Fga custom annotation with a {@code @Before} pointcut available for use in preauthorize. TODOs:
     * - Can we make some inferences and defaults for the current principal so we don't have to pass the user ID?
     * - Do we need more flexibility in the pointcut?
     * - would https://github.com/spring-projects/spring-security/issues/14480 make this implementation easier, and would it support SpEL for fields like object and userId
     */
    @FgaCheck(userType="user", userId="honest_user", relation="reader", objectType="document", object="#id")
    public Document getDocumentWithFgaCheck(String id) {
        return new Document(id, "You have reader access to this document!");
    }

    /**
     * Demonstrates a simple example of using the injected fgaClient to write authorization data to FGA.
     */
    public String createDoc(String id) {
        ClientWriteRequest writeRequest =  new ClientWriteRequest()
                .writes(List.of(new ClientTupleKey()
                        .user("user:123")
                        .relation("reader")
                        ._object(String.format("document:%s", id))));

        try {
            fgaClient.write(writeRequest).get();
        } catch (InterruptedException | ExecutionException | FgaInvalidParameterException e) {
            throw new RuntimeException("Error writing to FGA", e);
        }

        return String.format("Created doc with ID %s and associated user:123 as a reader FGA relationship", id);
    }
}
