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

    /**
     * Simple Fga bean available for use in preauthorize. TODOs:
     * - without any auth, we are just hard-coding the userId for simple demo purposes. Can we infer it from the principal in a real example?
     * - multiple params of same type with optional params (e.g., userId) could be unwieldy
     */
    @PreAuthorize("@openFga.check(#id, 'document', 'reader', 'user', '123')")
    @GetMapping("/docs/{id}")
    public String simpleBean(@PathVariable String id) {
        return "You have access!";
    }


    /**
     * Fga custom annotation with a {@code @Before} pointcut available for use in preauthorize. TODOs:
     * - Can we make some inferences and defaults for the current principal so we don't have to pass the user ID?
     * - Do we need more flexibility in the pointcut?
     * - would https://github.com/spring-projects/spring-security/issues/14480 make this implementation easier, and would it support SpEL for fields like object and userId
     */
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