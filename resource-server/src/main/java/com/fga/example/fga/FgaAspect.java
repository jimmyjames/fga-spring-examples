package com.fga.example.fga;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.client.model.ClientCheckResponse;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

@Aspect
@Component
public class FgaAspect {

    private final Logger logger = LoggerFactory.getLogger(FgaAspect.class);

    private final OpenFgaClient fgaClient;

    FgaAspect(OpenFgaClient fgaClient) {
        this.fgaClient = fgaClient;
    }

    @Before("@annotation(fga)")
    public void check(final JoinPoint jointPoint, final FgaCheck fga) {
        logger.debug("**** CUSTOM AOP CALLED *****");

        MethodSignature signature = (MethodSignature) jointPoint.getSignature();
        Method method = signature.getMethod();
        FgaCheck fgaCheckAnnotation = method.getAnnotation(FgaCheck.class);

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new
                StandardEvaluationContext();

        for (int i = 0; i < signature.getParameterNames().length; i++) {
            context.setVariable(signature.getParameterNames()[i], jointPoint.getArgs()[i]);
        }

        String obj = parser.parseExpression(fgaCheckAnnotation.object()).getValue(context, String.class);
        String relation = fgaCheckAnnotation.relation();
        String objType = fgaCheckAnnotation.objectType();
        String userType = fgaCheckAnnotation.userType();
        String userId;

        if (ObjectUtils.isEmpty(fgaCheckAnnotation.userId())) {
            // If no userId supplied, use the name of the current authentication principal
            userId = SecurityContextHolder.getContext().getAuthentication().getName();
        } else {
            userId = fgaCheckAnnotation.userId();
        }

        if (!fgaCheck(String.format("%s:%s", userType, userId), relation, String.format("%s:%s", objType, obj))) {
            throw new AccessDeniedException("Access Denied");
        }
    }

    private boolean fgaCheck(String user, String relation, String _object) {

        var body = new ClientCheckRequest()
                .user(user)
                .relation(relation)
                ._object(_object);

        ClientCheckResponse response = null;
        try {
            response = fgaClient.check(body).get();
        } catch (InterruptedException | FgaInvalidParameterException | ExecutionException e) {
            throw new RuntimeException("Error performing FGA check", e);
        }

        return response.getAllowed();
    }
}