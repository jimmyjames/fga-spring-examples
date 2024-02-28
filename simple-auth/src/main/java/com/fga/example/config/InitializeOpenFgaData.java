package com.fga.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadAuthorizationModelResponse;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.api.configuration.ClientReadAuthorizationModelOptions;
import dev.openfga.sdk.api.configuration.ClientWriteOptions;
import dev.openfga.sdk.api.model.CreateStoreRequest;
import dev.openfga.sdk.api.model.Store;
import dev.openfga.sdk.api.model.WriteAuthorizationModelRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Creates an FGA store, writes a simple authorization model, and adds a single tuple of form:
 * user: user:123
 * relation: can_read
 * object: document:1
 * <p>
 * This is for sample purposes only; would not be necessary in a real application.
 */
public class InitializeOpenFgaData implements ResourceLoaderAware, InitializingBean {

    Logger logger = LoggerFactory.getLogger(InitializeOpenFgaData.class);
    private volatile ResourceLoader resourceLoader;
    private final OpenFgaProperties openFgaProperties;
    private final OpenFgaClient openFgaClient;

    public InitializeOpenFgaData(OpenFgaProperties openFgaProperties, OpenFgaClient openFgaClient) {
        this.openFgaProperties = openFgaProperties;
        this.openFgaClient = openFgaClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        doInitialization();
    }

    private void doInitialization() throws Exception {
        if (StringUtils.hasText(openFgaProperties.getFgaStoreId())) {
            validateStore(openFgaProperties.getFgaStoreId());
            if (StringUtils.hasText(openFgaProperties.getFgaAuthorizationModelId())) {
                validateAuthorizationModelId(openFgaProperties.getFgaAuthorizationModelId());
            } else {
                var latestAuthModel = getLatestAuthorizationModelId();
                logger.debug("Authorization Check resolved with a Status Code of {} and {}", latestAuthModel.getStatusCode(), latestAuthModel.getAuthorizationModel());
                if (latestAuthModel.getStatusCode() == 200) {
                    openFgaProperties.setFgaAuthorizationModelId(latestAuthModel.getAuthorizationModel().getId());
                }
            }
        } else {
            // This is messy
            openFgaProperties.setFgaStoreId(makeStore());
            logger.debug("Failed to find an Authorization Model, checking for a Schema at {}", openFgaProperties.getFgaAuthModelSchema());
            var script = resourceLoader.getResource(openFgaProperties.getFgaAuthModelSchema());
            var mapper = new ObjectMapper().registerModule(new SimpleModule().addDeserializer(ClientTupleKey.class, new OpenFgaClientTupleKeyDeserializer())); // Wrong thing to do for a proper init, but this works for now.
            logger.trace("Writing the following Authorization Model Schema: \n{}", script.getContentAsString(StandardCharsets.UTF_8));
            var authWriteResponse = openFgaClient.writeAuthorizationModel(mapper.readValue(script.getContentAsByteArray(), WriteAuthorizationModelRequest.class)).get();
            logger.debug("Authorization Model Creation Request responded with a Status Code of {} and {}", authWriteResponse.getStatusCode(), authWriteResponse.getAuthorizationModelId());
            openFgaProperties.setFgaAuthorizationModelId(authWriteResponse.getAuthorizationModelId());
            for (String relationshipFile : openFgaProperties.getFgaInitialRelationshipTuple()) {
                Resource resource = resourceLoader.getResource(relationshipFile);
                logger.debug("Adding new Relationship Tuple, \n{}", resource.getContentAsString(StandardCharsets.UTF_8));
                var clientWriteRequest = mapper.readValue(resource.getContentAsByteArray(), ClientWriteRequest.class);
                setUpRelationshipTuples(clientWriteRequest);
            }
        }
    }

    private ClientReadAuthorizationModelResponse getLatestAuthorizationModelId() throws Exception { // Way too broad, but the fail cases are extreme in this.
        return openFgaClient.readLatestAuthorizationModel().get();
    }

    private void setUpRelationshipTuples(ClientWriteRequest request) {
        // Write
        try {
            logger.debug("Writing Tuples");
            openFgaClient
                    .write(request,
                            new ClientWriteOptions()
                                    .disableTransactions(true)
                                    .authorizationModelId(openFgaProperties.getFgaAuthorizationModelId()))
                    .get();
            logger.debug("Done Writing Tuples");
        } catch (Exception e) {
            logger.error("Failed to write {} due to Exception {}, application will now fail to start.", request, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void validateStore(String storeId) throws Exception {
        Objects.requireNonNull(storeId);
        var invalidStore = openFgaClient.listStores().get()
                .getStores()
                .stream()
                .map(Store::getId)
                .noneMatch(storeId::equals);
        if (invalidStore) {
            throw new IllegalArgumentException("The Store ID: " + storeId + " does not exist.");
        }
    }

    private void validateAuthorizationModelId(String authId) throws Exception {
        Objects.requireNonNull(authId);
        var storeAuthModelResponse = openFgaClient.readAuthorizationModel(new ClientReadAuthorizationModelOptions().authorizationModelId(authId))
                .get();
        logger.debug("Validating Authorization Model {}, Status Code {}, Response {}", authId, storeAuthModelResponse.getStatusCode(), storeAuthModelResponse.getAuthorizationModel());
        switch (HttpStatus.valueOf(storeAuthModelResponse.getStatusCode())) {
            case HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND ->
                    throw new IllegalStateException("Failed to find the Authorization Model for " + authId);
            case HttpStatus.CONFLICT ->
                    throw new IllegalStateException("Transaction Conflict within OpenFGA for Authorization Model:  " + authId);
            case HttpStatus.INTERNAL_SERVER_ERROR ->
                    throw new IllegalStateException("OpenFGA Server had an internal failure when checking for the Authorization Model of " + authId);
        }
    }

    private String makeStore() {
        String storeName = openFgaProperties.getFgaStoreName();
        Objects.requireNonNull(storeName, "Failed to have a Store ID or Store Name provided, OpenFGA will fail to start.");
        String newStoreId;
        try {
            logger.debug("Created a new Store with the name {}", storeName);
            var newStore = openFgaClient.createStore(new CreateStoreRequest().name(storeName)).get();
            logger.debug("Store Name {}, Status Code {}, Response {}", storeName, newStore.getStatusCode(), newStore.getRawResponse());
            openFgaClient.setStoreId(newStore.getId());
            newStoreId = newStore.getId();
        } catch (Exception e) {
            logger.error("Failed to create a new OpenFGA Store", e);
            throw new RuntimeException(e);
        }
        return newStoreId;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
