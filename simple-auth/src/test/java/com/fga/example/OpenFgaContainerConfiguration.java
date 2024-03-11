package com.fga.example;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.openfga.OpenFGAContainer;

@TestConfiguration(proxyBeanMethods = false)
class OpenFgaContainerConfiguration {

	@Bean
	@ServiceConnection
	OpenFGAContainer openFgaContainer() {
		return new OpenFGAContainer("openfga/openfga:v1.4.3");
	}

}
