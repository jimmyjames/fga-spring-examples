package org.example;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.client.model.ClientCheckResponse;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

/**
 * Simple bean that can be used to perform an FGA check.
 */
@Component
public class OpenFga {

    private final OpenFgaClient fgaClient;

    public OpenFga(OpenFgaClient fgaClient) {
        this.fgaClient = fgaClient;
    }
    public boolean check(String user, String relation, String _object) {

        var body = new ClientCheckRequest()
                .user(user)
                .relation(relation)
                ._object(_object);

        ClientCheckResponse response = null;
        try {
            response = fgaClient.check(body).get();
        } catch (InterruptedException | FgaInvalidParameterException | ExecutionException e) {
            throw new RuntimeException("Error performing FGA check", e);
        }

        return response.getAllowed();
    }
}
