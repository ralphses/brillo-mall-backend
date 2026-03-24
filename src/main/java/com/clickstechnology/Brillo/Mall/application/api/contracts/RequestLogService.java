package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.enums.RequestStatus;

public interface RequestLogService {
    String logRequest(Object request, String origin, String userDevice);
    void update(String logId, Object response, RequestStatus status);
}
