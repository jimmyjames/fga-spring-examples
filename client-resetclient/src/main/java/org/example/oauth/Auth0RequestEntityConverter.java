package org.example.oauth;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

// TODO
//  - Not FGA-related
//  - Auth0 requires an audience parameter on the authorization request to issue a JWT
//  - There HAS to be a better way to add a single parameter instead of building the entire request!?!
//  - See https://stackoverflow.com/questions/72362533/oauth2-client-with-extra-parameters-in-body-audience
class Auth0RequestEntityConverter implements
        Converter<OAuth2ClientCredentialsGrantRequest, RequestEntity<?>> {

    private final String audience;

    Auth0RequestEntityConverter(String audience) {
        this.audience = audience;
    }

    @Override
    public RequestEntity<?> convert(OAuth2ClientCredentialsGrantRequest clientCredentialsGrantRequest) {
        var clientRegistration = clientCredentialsGrantRequest.getClientRegistration();

        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8"));
        if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.equals(clientRegistration.getClientAuthenticationMethod())) {
            headers.setBasicAuth(clientRegistration.getClientId(), clientRegistration.getClientSecret());
        }
        var formParameters = this.buildFormParameters(clientCredentialsGrantRequest);

        var uri = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getTokenUri())
                .build()
                .toUri();
        return new RequestEntity<>(formParameters, headers, HttpMethod.POST, uri);
    }

    private Object buildFormParameters(OAuth2ClientCredentialsGrantRequest clientCredentialsGrantRequest) {
        var clientRegistration = clientCredentialsGrantRequest.getClientRegistration();
        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        formParameters.add(OAuth2ParameterNames.GRANT_TYPE, clientCredentialsGrantRequest.getGrantType().getValue());

        if (!CollectionUtils.isEmpty(clientRegistration.getScopes())) {
            formParameters.add(OAuth2ParameterNames.SCOPE,
                    StringUtils.collectionToDelimitedString(clientRegistration.getScopes(), " "));
        }

        if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.equals(clientRegistration.getClientAuthenticationMethod())) {
            formParameters.add(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId());
            formParameters.add(OAuth2ParameterNames.CLIENT_SECRET, clientRegistration.getClientSecret());
        }

        formParameters.add("audience", audience);
        return formParameters;
    }

}