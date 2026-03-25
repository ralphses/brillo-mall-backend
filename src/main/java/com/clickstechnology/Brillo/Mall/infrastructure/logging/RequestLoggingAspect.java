package com.clickstechnology.Brillo.Mall.infrastructure.logging;

import com.clickstechnology.Brillo.Mall.application.enums.RequestStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheNames;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestLoggingAspect {

    private final CacheUtil cacheUtil;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;

    private static final ThreadLocal<String> logContext = new ThreadLocal<>();

    @Around("@annotation(LoggableRequest)")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {

        String logId = UUID.randomUUID().toString();
        logContext.set(logId);

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

        } finally {
            logContext.remove();
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
            return objectMapper.writeValueAsString(obj);
        } catch (Exception ex) {
            log.warn("Failed to serialize object for logging", ex);
            return String.valueOf(obj);
        }
    }
}
