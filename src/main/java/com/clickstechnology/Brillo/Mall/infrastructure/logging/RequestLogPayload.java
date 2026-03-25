package com.clickstechnology.Brillo.Mall.infrastructure.logging;


import com.clickstechnology.Brillo.Mall.application.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestLogPayload {

    private String id;

    private String requestBody;
    private String responseBody;

    private String ipAddress;
    private String userAgent;

    private RequestStatus status;

    public static RequestLogPayload update(String id, String response, RequestStatus status) {
        return RequestLogPayload.builder()
                .id(id)
                .responseBody(response)
                .status(status)
                .build();
    }
}
