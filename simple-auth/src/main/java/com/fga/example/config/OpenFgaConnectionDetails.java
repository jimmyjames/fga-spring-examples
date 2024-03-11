package com.fga.example.config;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface OpenFgaConnectionDetails extends ConnectionDetails {

    String getFgaApiUrl();

}
