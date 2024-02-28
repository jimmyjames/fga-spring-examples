package com.fga.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix="openfga")
public class OpenFgaProperties {

    private String fgaApiUrl;
    private String fgaStoreId;
    private String fgaStoreName;
    private String fgaApiTokenIssuer;
    private String fgaApiAudience;
    private String fgaClientId;
    private String fgaClientSecret;
    private String fgaAuthorizationModelId;
    private String fgaAuthModelSchema;
    private List<String> fgaInitialRelationshipTuple;

    private static final String FGA_MODEL_SCHEMA_DEFAULT = "classpath:data/openfga-schema.json";
    private static final List<String> FGA_RELATIONSHIP_TUPLE_DEFAULT = List.of("classpath:data/openfga-tuple.json");

    public String getFgaStoreName() {
        return fgaStoreName;
    }

    public void setFgaStoreName(String fgaStoreName) {
        this.fgaStoreName = fgaStoreName;
    }

    public String getFgaAuthModelSchema() {
        return fgaAuthModelSchema == null ? FGA_MODEL_SCHEMA_DEFAULT : fgaAuthModelSchema;
    }

    public void setFgaAuthModelSchema(String fgaAuthModelSchema) {
        this.fgaAuthModelSchema = fgaAuthModelSchema;
    }

    public String getFgaApiUrl() {
        return fgaApiUrl;
    }

    public void setFgaApiUrl(String fgaApiUrl) {
        this.fgaApiUrl = fgaApiUrl;
    }

    public String getFgaStoreId() {
        return fgaStoreId;
    }

    public void setFgaStoreId(String fgaStoreId) {
        this.fgaStoreId = fgaStoreId;
    }

    public String getFgaApiTokenIssuer() {
        return fgaApiTokenIssuer;
    }

    public void setFgaApiTokenIssuer(String fgaApiTokenIssuer) {
        this.fgaApiTokenIssuer = fgaApiTokenIssuer;
    }

    public String getFgaApiAudience() {
        return fgaApiAudience;
    }

    public void setFgaApiAudience(String fgaApiAudience) {
        this.fgaApiAudience = fgaApiAudience;
    }

    public String getFgaClientId() {
        return fgaClientId;
    }

    public void setFgaClientId(String fgaClientId) {
        this.fgaClientId = fgaClientId;
    }

    public String getFgaClientSecret() {
        return fgaClientSecret;
    }

    public void setFgaClientSecret(String fgaClientSecret) {
        this.fgaClientSecret = fgaClientSecret;
    }

    public String getFgaAuthorizationModelId() {
        return fgaAuthorizationModelId;
    }

    public void setFgaAuthorizationModelId(String fgaAuthorizationModelId) {
        this.fgaAuthorizationModelId = fgaAuthorizationModelId;
    }

    public List<String> getFgaInitialRelationshipTuple() {
        return fgaInitialRelationshipTuple == null ? FGA_RELATIONSHIP_TUPLE_DEFAULT : fgaInitialRelationshipTuple;
    }

    public void setFgaInitialRelationshipTuple(List<String> fgaInitialRelationshipTuple) {
        this.fgaInitialRelationshipTuple = fgaInitialRelationshipTuple;
    }
}
