package com.clickstechnology.Brillo.Mall.infrastructure.logging;

import com.clickstechnology.Brillo.Mall.application.api.contracts.RequestLogService;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheNames;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLogScheduler {

    private final CacheUtil cacheUtil;
    private final RequestLogService requestLogService;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 200;

    @Scheduled(fixedDelay = 60_000)
    public void persistLogs() {

        int totalProcessed = 0;

        while (true) {

            List<Object> batch = cacheUtil.popBatch(CacheNames.REQUEST_LOG_QUEUE, BATCH_SIZE);

            if (batch.isEmpty()) {
                break;
            }

            try {
                List<RequestLogPayload> logs = batch.stream()
                        .map(this::convertToPayload)
                        .toList();

                requestLogService.saveAll(logs);
                totalProcessed += logs.size();

            } catch (Exception e) {
                log.error("Failed to persist request logs batch", e);

                batch.forEach(obj ->
                        cacheUtil.pushToList(CacheNames.REQUEST_LOG_QUEUE, obj)
                );

                break;
            }
        }

        if (totalProcessed > 0) {
            log.info("Persisted {} request logs to DB", totalProcessed);
        }
    }

    private RequestLogPayload convertToPayload(Object obj) {
        if (obj instanceof RequestLogPayload payload) {
            return payload;
        }
        return objectMapper.convertValue(obj, RequestLogPayload.class);
    }
}
