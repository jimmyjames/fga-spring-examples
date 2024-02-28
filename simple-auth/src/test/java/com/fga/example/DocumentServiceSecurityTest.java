package com.fga.example;

import com.fga.example.service.DocumentService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;


import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(OpenFgaContainerConfiguration.class)
class DocumentServiceSecurityTest {

	public static final String DOCUMENT_GRANTED_ID = "1";

	public static final String DOCUMENT_DENIED_ID = "2";

	@Test
	@WithHonestUser
	void preAuthorizeWhenGranted(@Autowired DocumentService documentService) {
		assertThatCode(() -> documentService.getDocumentWithPreAuthorize(DOCUMENT_GRANTED_ID))
				.doesNotThrowAnyException();
	}

	@Test
	@WithHonestUser
	void preAuthorizeWhenNoDocumentThenDenied(@Autowired DocumentService documentService) {
		assertThatExceptionOfType(AccessDeniedException.class)
				.isThrownBy(() -> documentService.getDocumentWithPreAuthorize(DOCUMENT_DENIED_ID));
	}

	@Test
	@WithEvilUser
	void preAuthorizeWhenWrongUserThenDenied(@Autowired DocumentService documentService) {
		assertThatExceptionOfType(AccessDeniedException.class)
				.isThrownBy(() -> documentService.getDocumentWithPreAuthorize(DOCUMENT_DENIED_ID));
	}

	@Test
	@WithHonestUser
	void preOpenFgaCheckWhenGranted(@Autowired DocumentService documentService) {
		assertThatCode(() -> documentService.getDocumentWithPreOpenFgaCheck(DOCUMENT_GRANTED_ID))
				.doesNotThrowAnyException();
	}

	@Test
	@WithHonestUser
	void preOpenFgaCheckWhenNoDocumentThenDenied(@Autowired DocumentService documentService) {
		assertThatExceptionOfType(AccessDeniedException.class)
				.isThrownBy(() -> documentService.getDocumentWithPreOpenFgaCheck(DOCUMENT_DENIED_ID));
	}

	@Test
	@WithEvilUser
	void preOpenFgaCheckWhenWrongUserDenied(@Autowired DocumentService documentService) {
		assertThatExceptionOfType(AccessDeniedException.class)
				.isThrownBy(() -> documentService.getDocumentWithPreOpenFgaCheck(DOCUMENT_DENIED_ID));
	}

	@Test
	@WithHonestUser
	void preReadDocumentCheckWhenGranted(@Autowired DocumentService documentService) {
		assertThatCode(() -> documentService.getDocumentWithPreReadDocumentCheck(DOCUMENT_GRANTED_ID))
				.doesNotThrowAnyException();
	}

	@Test
	@WithEvilUser
	void preReadDocumentCheckWhenDenied(@Autowired DocumentService documentService) {
		Assertions.setMaxStackTraceElementsDisplayed(Integer.MAX_VALUE);
		assertThatExceptionOfType(AccessDeniedException.class)
				.isThrownBy(() -> documentService.getDocumentWithPreReadDocumentCheck(DOCUMENT_DENIED_ID));
	}

	@Test
	void fgaCheckWhenGranted(@Autowired DocumentService documentService) {
		assertThatCode(() -> documentService.getDocumentWithFgaCheck(DOCUMENT_GRANTED_ID))
				.doesNotThrowAnyException();
	}

	@Test
	void fgaCheckWhenDenied(@Autowired DocumentService documentService) {
		assertThatExceptionOfType(AccessDeniedException.class)
				.isThrownBy(() -> documentService.getDocumentWithFgaCheck(DOCUMENT_DENIED_ID));
	}

}
