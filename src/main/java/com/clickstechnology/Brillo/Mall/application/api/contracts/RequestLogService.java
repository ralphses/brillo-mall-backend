package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.enums.RequestStatus;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.RequestLogPayload;

import java.util.List;

public interface RequestLogService {
    String logRequest(String request, String origin, String userDevice);
    void update(String logId, String response, RequestStatus status);

    void saveAll(List<RequestLogPayload> logs);
}
