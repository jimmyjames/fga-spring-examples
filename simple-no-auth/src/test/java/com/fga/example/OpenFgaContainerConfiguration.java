package com.fga.example;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

@TestConfiguration(proxyBeanMethods = false)
class OpenFgaContainerConfiguration {

	@Bean
	GenericContainer<?> openFgaContainer(DynamicPropertyRegistry registry) {
		var result = new GenericContainer<>("openfga/openfga:v1.4.3")
				.withCommand("run")
				.waitingFor(Wait.forHttp("/playground").forPort(3000).withStartupTimeout(Duration.ofMinutes(2)))
				.withEnv("OPENFGA_HTTP_ADDR","0.0.0.0:4000")
				.withExposedPorts(4000, 8081, 3000);
		registry.add("openfga.fgaApiUrl", () -> {
			Integer httpPort = result.getMappedPort(4000);
			return "http://localhost:"+httpPort;
		});
		return result;
	}

}
