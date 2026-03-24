package com.clickstechnology.Brillo.Mall.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {
    private T data;
    private String message;
    private int status;
}
