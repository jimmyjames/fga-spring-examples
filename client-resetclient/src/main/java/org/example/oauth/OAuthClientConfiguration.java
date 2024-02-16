package org.example.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.MultiValueMap;

/**
 * Configures Spring for the client credentials grant using Auth0
 */
@Configuration
public class OAuthClientConfiguration {

    @Value("${auth0-audience}")
    private String audience;

    @Bean
    ClientRegistration oktaClientRegistration(
            @Value("${spring.security.oauth2.client.provider.okta.token-uri}") String token_uri,
            @Value("${spring.security.oauth2.client.registration.okta.client-id}") String client_id,
            @Value("${spring.security.oauth2.client.registration.okta.client-secret}") String client_secret,
            @Value("${spring.security.oauth2.client.registration.okta.authorization-grant-type}") String authorizationGrantType
    ) {
        return ClientRegistration
                .withRegistrationId("okta")
                .tokenUri(token_uri)
                .clientId(client_id)
                .clientSecret(client_secret)
                .authorizationGrantType(new AuthorizationGrantType(authorizationGrantType))
                .build();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(ClientRegistration oktaClientRegistration) {
        return new InMemoryClientRegistrationRepository(oktaClientRegistration);
    }

    @Bean
    public OAuth2AuthorizedClientService auth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    public AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceAndManager (
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials(clientCredentialsGrantBuilder -> {
                            var clientCredentialsTokenResponseClient = new DefaultClientCredentialsTokenResponseClient();
                            clientCredentialsTokenResponseClient.setRequestEntityConverter(auth0RequestConverter());
                            clientCredentialsGrantBuilder.accessTokenResponseClient(clientCredentialsTokenResponseClient);
                        })
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    /**
     * Adds the {@code audience} parameter to the authorization request, which is required by
     * Auth0 to obtain a JWT for the specified API audience.
     * @return an {@link OAuth2ClientCredentialsGrantRequestEntityConverter} instance that adds the
     *         {@code audience} parameter
     */
    private OAuth2ClientCredentialsGrantRequestEntityConverter auth0RequestConverter() {
        return new OAuth2ClientCredentialsGrantRequestEntityConverter() {
            @Override
            protected MultiValueMap<String, String> createParameters(
                    OAuth2ClientCredentialsGrantRequest clientCredentialsGrantRequest) {

                var parameters = super.createParameters(clientCredentialsGrantRequest);
                parameters.add("audience", audience);
                return parameters;
            }
        };
    }
}
