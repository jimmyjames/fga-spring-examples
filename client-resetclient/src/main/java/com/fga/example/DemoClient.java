package com.fga.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.RestClient;

import java.util.Objects;
import java.util.Random;

@SpringBootApplication
public class DemoClient implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(DemoClient.class);
    private final AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceAndManager;

    public static void main(String[] args) {
        SpringApplication.run(DemoClient.class, args);
    }

    public DemoClient(AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceAndManager) {
        this.authorizedClientServiceAndManager = authorizedClientServiceAndManager;
    }

    @Override
    public void run(String... args) throws Exception {

        // Build an OAuth2 request for the Okta provider
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("okta")
                .principal("Demo Service")
                .build();

        // make authorization request to retrieve JWT from Auth0
        OAuth2AuthorizedClient authorizedClient = this.authorizedClientServiceAndManager.authorize(authorizeRequest);
        OAuth2AccessToken accessToken = Objects.requireNonNull(authorizedClient).getAccessToken();

        logger.info("Obtained token: " + accessToken.getTokenValue());

        // Configure RestClient to send the JWT as an Authorization: Bearer header on requests
        RestClient restClient = RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + accessToken.getTokenValue())
                .build();

        // generate a random ID to use when making requests
        Random random = new Random();
        String randomId = String.valueOf(Math.abs(random.nextInt()));

        // try and retrieve a doc for the principal who does not have access, protected by JWT and FGA
        logger.info("Will call resource server to get document with ID " + randomId + ", should **NOT** have FGA access");
        String responseNotExpected = restClient.get()
                .uri("http://localhost:8082/docsaop/" + randomId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    logger.info("====> HTTP Error! Server returned response code " + response.getStatusCode());
                })
                .body(String.class);
        if (responseNotExpected != null) {
            logger.error("====> Should not have received successful response: " + responseNotExpected);
        }


        // create a doc and write an FGA tuple representing the relationship
        logger.info("Will call resource server to create a document with ID " + randomId + ", and write FGA authorization data");
        String writeResponse = restClient.post()
                .uri("http://localhost:8082/docs")
                .body(randomId) // doc ID
                .retrieve()
                .body(String.class);
        logger.info("====> Response from creating document: " + writeResponse);

        // try and retrieve doc just created, protected by JWT and FGA
        logger.info("Will call resource server to get document with ID " + randomId + ", should now have FGA access");
        String readResponse = restClient.get()
                .uri("http://localhost:8082/docsaop/" + randomId)
                .retrieve()
                .body(String.class);
        logger.info("====> Response from getting document for which principal has access: " + readResponse);
    }
}