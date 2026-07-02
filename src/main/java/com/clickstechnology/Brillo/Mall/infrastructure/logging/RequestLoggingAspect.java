package com.clickstechnology.Brillo.Mall.infrastructure.logging;

import com.clickstechnology.Brillo.Mall.application.enums.RequestStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheNames;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestLoggingAspect {

    private static final List<String> NOISY_PATH_PREFIXES = List.of(
            "/actuator",
            "/swagger-ui",
            "/swagger-resources",
            "/v3/api-docs",
            "/webjars"
    );

    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Set.of(
            "password",
            "currentPassword",
            "newPassword",
            "oldPassword",
            "confirmPassword",
            "token",
            "accessToken",
            "refreshToken",
            "authorization",
            "secret",
            "apiKey",
            "otp",
            "pin",
            "cvv",
            "cardNumber"
    ));

    private final CacheUtil cacheUtil;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;

    @Around("@annotation(LoggableRequest)")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        if (shouldSkipLogging()) {
            return joinPoint.proceed();
        }

        String logId = UUID.randomUUID().toString();
        Object requestBody = extractRequestBody(joinPoint.getArgs());

        String ipAddress = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");

        String safeRequest = safeToJson(requestBody);

        cacheUtil.pushToList(
                CacheNames.REQUEST_LOG_QUEUE,
                RequestLogPayload.builder()
                        .id(logId)
                        .requestBody(safeRequest)
                        .ipAddress(ipAddress)
                        .userAgent(userAgent)
                        .status(RequestStatus.INITIATED)
                        .build()
        );

        try {
            Object result = joinPoint.proceed();

            String safeResponse = safeToJson(result);

            cacheUtil.pushToList(
                    CacheNames.REQUEST_LOG_QUEUE,
                    RequestLogPayload.update(logId, safeResponse, RequestStatus.PROCESSED)
            );

            return result;

        } catch (BusinessException ex) {

            cacheUtil.pushToList(
                    CacheNames.REQUEST_LOG_QUEUE,
                    RequestLogPayload.update(logId, ex.getMessage(), RequestStatus.FAILED)
            );

            throw ex;

        } catch (Exception ex) {

            cacheUtil.pushToList(
                    CacheNames.REQUEST_LOG_QUEUE,
                    RequestLogPayload.update(logId, "Internal server error", RequestStatus.FAILED)
            );

            log.error("Request processing failed", ex);
            throw ex;
        }
    }

    private Object extractRequestBody(Object[] args) {
        return Arrays.stream(args)
                .filter(arg -> !(arg instanceof HttpServletRequest))
                .findFirst()
                .orElse(null);
    }

    private String safeToJson(Object obj) {
        try {
            if (obj == null) return null;
            JsonNode node = objectMapper.valueToTree(obj);
            redact(node);
            return objectMapper.writeValueAsString(node);
        } catch (Exception ex) {
            log.debug("Failed to serialize object for request logging: {}", ex.getMessage());
            return String.valueOf(obj);
        }
    }

    private boolean shouldSkipLogging() {
        String requestUri = httpServletRequest.getRequestURI();
        if (requestUri == null) {
            return false;
        }

        return NOISY_PATH_PREFIXES.stream()
                .anyMatch(requestUri::startsWith);
    }

    private void redact(JsonNode node) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            List<String> fieldNames = new ArrayList<>();
            objectNode.fieldNames().forEachRemaining(fieldNames::add);

            for (String fieldName : fieldNames) {
                JsonNode childNode = objectNode.get(fieldName);
                if (isSensitive(fieldName)) {
                    objectNode.put(fieldName, "***");
                } else {
                    redact(childNode);
                }
            }
            return;
        }

        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode child : arrayNode) {
                redact(child);
            }
        }
    }

    private boolean isSensitive(String fieldName) {
        if (fieldName == null) {
            return false;
        }

        String normalized = fieldName.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        return SENSITIVE_FIELDS.stream()
                .map(value -> value.replaceAll("[^A-Za-z0-9]", "").toLowerCase())
                .anyMatch(normalized::contains);
    }
}
