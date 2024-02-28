package com.fga.example;

import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@WithMockUser("evil_user")
@Retention(RetentionPolicy.RUNTIME)
public @interface WithEvilUser {
}
