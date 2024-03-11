package com.fga.example.service.connection;

import com.fga.example.config.OpenFgaConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.testcontainers.openfga.OpenFGAContainer;

class OpenFgaContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<OpenFGAContainer, OpenFgaConnectionDetails> {

	@Override
	protected OpenFgaConnectionDetails getContainerConnectionDetails(
			ContainerConnectionSource<OpenFGAContainer> source) {
		return new OpenFgaContainerConnectionDetails(source);
	}

	private static final class OpenFgaContainerConnectionDetails extends ContainerConnectionDetails<OpenFGAContainer>
			implements OpenFgaConnectionDetails {

		private OpenFgaContainerConnectionDetails(ContainerConnectionSource<OpenFGAContainer> source) {
			super(source);
		}

		@Override
		public String getFgaApiUrl() {
			return getContainer().getHttpEndpoint();
		}
	}

}
