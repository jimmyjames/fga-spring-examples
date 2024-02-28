package com.fga.example.fga;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Order(Ordered.HIGHEST_PRECEDENCE)
@PostAuthorize("@openFga.check({object}, {objectType}, {relation}, {userType}, {userId})")
public @interface PostOpenFgaCheck {
    String object();
    String relation();
    String userType();
    String objectType();

    String userId() default "authentication.name";
}
