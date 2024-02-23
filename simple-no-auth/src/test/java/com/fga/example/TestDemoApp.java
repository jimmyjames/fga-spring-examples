package com.fga.example;

import org.springframework.boot.SpringApplication;


class TestDemoApp {

	public static void main(String[] args) {
		SpringApplication
			.from(DemoApp::main)
			.with(OpenFgaContainerConfiguration.class)
			.run(args);
	}
}