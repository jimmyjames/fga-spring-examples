package com.fga.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.api.configuration.ClientWriteOptions;
import dev.openfga.sdk.api.model.CreateStoreRequest;
import dev.openfga.sdk.api.model.WriteAuthorizationModelRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Creates an FGA store, writes a simple authorization model, and adds a single tuple of form:
 * user: user:123
 * relation: can_read
 * object: document:1
 *
 * This is for sample purposes only; would not be necessary in a real application.
 */
@Configuration
public class LoadFgaData implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(LoadFgaData.class);
    private final OpenFgaClient fgaClient;
    private final ObjectMapper objectMapper;

    LoadFgaData(OpenFgaClient fgaClient, ObjectMapper objectMapper) {
        this.fgaClient = fgaClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        // CreateStore
        logger.debug("Creating Test Store");
        var store = fgaClient
                .createStore(new CreateStoreRequest().name("Test Store"))
                .get();
        logger.debug("Test Store ID: " + store.getId());

        // Set the store id
        fgaClient.setStoreId(store.getId());

        // ListStores after Create
        logger.debug("Listing Stores");
        var stores = fgaClient.listStores().get();
        logger.debug("Stores Count: " + stores.getStores().size());

        // GetStore
        logger.debug("Getting Current Store");
        var currentStore = fgaClient.getStore().get();
        logger.debug("Current Store Name: " + currentStore.getName());


        var authModelJson = loadResource("example-auth-model.json");
        var authorizationModel = fgaClient
                .writeAuthorizationModel(objectMapper.readValue(authModelJson, WriteAuthorizationModelRequest.class))
                .get();
        logger.debug("Authorization Model ID " + authorizationModel.getAuthorizationModelId());

        // Set the model ID
        fgaClient.setAuthorizationModelId(authorizationModel.getAuthorizationModelId());

        // Write
        logger.debug("Writing Tuples");
        fgaClient
                .write(
                        new ClientWriteRequest()
                                .writes(List.of(new ClientTupleKey()
                                        .user("user:123")
                                        .relation("reader")
                                        ._object("document:1"))),
                        new ClientWriteOptions()
                                .disableTransactions(true)
                                .authorizationModelId(authorizationModel.getAuthorizationModelId()))
                .get();
        logger.debug("Done Writing Tuples");

        // Read
        logger.debug("Reading Tuples");
        var readTuples = fgaClient.read(new ClientReadRequest()).get();
        logger.debug("Read Tuples" + objectMapper.writeValueAsString(readTuples));

    }

    private String loadResource(String filename) {
        try {
            var filepath = Paths.get("src", "main", "resources", filename);
            return Files.readString(filepath, StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to load resource: " + filename, ioe);
        }
    }
}
