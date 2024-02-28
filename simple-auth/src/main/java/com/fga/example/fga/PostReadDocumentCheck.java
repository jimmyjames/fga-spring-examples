package com.fga.example.fga;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@PostOpenFgaCheck(userType="'user'", relation="'reader'", objectType="'document'", object="")
public @interface PostReadDocumentCheck {
	@AliasFor(attribute = "object", annotation = PostOpenFgaCheck.class)
	String value();
}
