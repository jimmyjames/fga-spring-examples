package com.fga.example;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.openfga.OpenFGAContainer;

@TestConfiguration(proxyBeanMethods = false)
class OpenFgaContainerConfiguration {

	@Bean
	OpenFGAContainer openFgaContainer(DynamicPropertyRegistry registry) {
		var result = new OpenFGAContainer("openfga/openfga:v1.4.3");
		registry.add("openfga.fgaApiUrl", result::getHttpEndpoint);
		return result;
	}

}
