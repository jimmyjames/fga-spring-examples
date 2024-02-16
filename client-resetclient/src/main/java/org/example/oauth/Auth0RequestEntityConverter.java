package org.example.oauth;

import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequestEntityConverter;
import org.springframework.util.MultiValueMap;

/**
 * Adds the {@code audience} parameter to the authorization request, as required by Auth0 to obtain a
 * JWT for the specified API.
 */
public class Auth0RequestEntityConverter extends OAuth2ClientCredentialsGrantRequestEntityConverter {

    private final String audience;

    Auth0RequestEntityConverter(String audience) {
        this.audience = audience;
    }

    @Override
    protected MultiValueMap<String, String> createParameters(
            OAuth2ClientCredentialsGrantRequest clientCredentialsGrantRequest) {

        var parameters = super.createParameters(clientCredentialsGrantRequest);
        parameters.add("audience", audience);
        return parameters;
    }

}
