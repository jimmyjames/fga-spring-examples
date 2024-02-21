package com.fga.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestConfiguration(proxyBeanMethods = false)
class TestDemoApp {

	@Bean
	GenericContainer<?> openFgaContainer(DynamicPropertyRegistry registry) {
		System.out.println("Starting");
		var result = new GenericContainer<>("openfga/openfga:latest")
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

	public static void main(String[] args) {
		SpringApplication
			.from(DemoApp::main)
			.with(TestDemoApp.class)
			.run(args);
	}
}