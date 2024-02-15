package org.example;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
@RestController
@EnableMethodSecurity
public class DemoApp {

    // configured OpenFgaClient can be injected to interact with the API directly, e.g., write data
    private final OpenFgaClient fgaClient;

    public DemoApp(OpenFgaClient fgaClient) {
        this.fgaClient = fgaClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApp.class);
    }

    /******************************************************************************************************************
     * Simple Fga bean available for use in preauthorize. TODOs:
     * - do we always need to hard-code the user ID or can we make some inferences and defaults for the current principal?
     * - SpEL to create object (object type : id) isn't very friendly
     ******************************************************************************************************************/
    @PreAuthorize("@openFga.check('user:123', 'reader', 'document:'.concat(#id))")
    @GetMapping("/docs/{id}")
    public String simpleBean(@PathVariable String id) {
        return "You have access!";
    }


    /******************************************************************************************************************
     * Fga custom annotation with a {@code @Before} pointcut available for use in preauthorize. TODOs:
     * - Can we make some inferences and defaults for the current principal so we don't have to pass the user ID?
     * - Do we need more flexibility in the pointcut?
     ******************************************************************************************************************/
    @FgaCheck(userType="user", userId="123", relation="reader", objectType="document", object="#id")
    @GetMapping("/docsaop/{id}")
    public String customAnnotation(@PathVariable String id) {
        return "You have access!";
    }


    /**
     * Demonstrates a simple example of using the injected fgaClient to write authorization data to FGA.
     */
    @PostMapping("/docs")
    public String createDoc(@RequestBody String id) {
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

        return String.format("Created doc with ID %s\n", id);
    }
}