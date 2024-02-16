package org.example;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.example.fga.FgaCheck;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
@RestController
public class DemoServer {

    private final OpenFgaClient fgaClient;

    public DemoServer(OpenFgaClient fgaClient) {
        this.fgaClient = fgaClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoServer.class, args);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(req -> req.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(AbstractHttpConfigurer::disable)
                .build();

    }

    @GetMapping("/")
    public String getMessage(Principal principal) {
        return "Welcome, " + principal.getName();
    }

    /**
     * Simple Fga bean available for use in preauthorize. TODOs:
     * - If userId is not specified, is it a good default to use the current principal name?
     * - multiple params with optional params (e.g., userId) could be unwieldy
     */
    @PreAuthorize("@openFga.check('#id', 'document', 'reader', 'user')")
    @GetMapping("/docsbean/{id}")
    public String simpleBean(@PathVariable String id) {
        return "You have access!";
    }

    /**
     * Fga custom annotation with a {@code @Before} pointcut available for use in preauthorize. TODOs:
     * - If userId is not specified, is it a good default to use the current principal name?
     * - Do we need more flexibility in the pointcut?
     * - would https://github.com/spring-projects/spring-security/issues/14480 make this implementation easier, and would it support SpEL for fields like object and userId
     */
    @FgaCheck(userType="user", relation="reader", objectType="document", object="#id")
    @GetMapping("/docsaop/{id}")
    public String customAnnotation(@PathVariable String id) {
        return "You have access!";
    }

    /**
     * Demonstrates a simple example of using the injected fgaClient to write authorization data to FGA.
     */
    @PostMapping("/docs")
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