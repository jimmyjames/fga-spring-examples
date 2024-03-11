package com.fga.example.config;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.configuration.ClientConfiguration;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Creates an {@link OpenFgaClient} configured from application properties
 */
@AutoConfiguration
@EnableConfigurationProperties(OpenFgaProperties.class)
public class OpenFgaAutoConfig {

    private final OpenFgaProperties openFgaProperties;

    public OpenFgaAutoConfig(OpenFgaProperties openFgaProperties) {
        this.openFgaProperties = openFgaProperties;
    }

    @Bean
    @ConditionalOnMissingBean(OpenFgaConnectionDetails.class)
    public PropertiesOpenFgaConnectionDetails openFgaConnectionDetails() {
        return new PropertiesOpenFgaConnectionDetails(this.openFgaProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientConfiguration openFgaConfig(OpenFgaConnectionDetails connectionDetails) {

        // For simplicity, this creates a client with NO AUTHENTICATION. It is NOT SUITABLE FOR PRODUCTION USE!!
        return new ClientConfiguration()
                .apiUrl(connectionDetails.getFgaApiUrl())
                .storeId(openFgaProperties.getFgaStoreId())
                .authorizationModelId(openFgaProperties.getFgaAuthorizationModelId());
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenFgaClient openFgaClient(ClientConfiguration configuration) {
        try {
            return new OpenFgaClient(configuration);
        } catch (FgaInvalidParameterException e) {
            // TODO how to best handle
            throw new RuntimeException(e);
        }
    }

    private static class PropertiesOpenFgaConnectionDetails implements OpenFgaConnectionDetails {

        private final OpenFgaProperties openFgaProperties;

        public PropertiesOpenFgaConnectionDetails(OpenFgaProperties openFgaProperties) {
            this.openFgaProperties = openFgaProperties;
        }

        @Override
        public String getFgaApiUrl() {
            return this.openFgaProperties.getFgaApiUrl();
        }
    }

}
