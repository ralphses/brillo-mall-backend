package com.clickstechnology.Brillo.Mall.application.dto.response;

public final class ResponseBuilder {
    private ResponseBuilder() {}

    public static <T> ApiResponse<T> success(T response) {
        return ApiResponse.<T>builder()
                .data(response)
                .message("success")
                .status(200)
                .build();
    }

    public static ApiResponse<String> success() {
        return ApiResponse.<String>builder()
                .data("Operation successful!")
                .message("success")
                .status(200)
                .build();
    }

    public static <T> ApiResponse<T> success(T response, String message) {
        return ApiResponse.<T>builder()
                .data(response)
                .message(message)
                .status(200)
                .build();
    }

    public static <T> ApiResponse<T> success(T response, String message, int status) {
        return ApiResponse.<T>builder()
                .data(response)
                .message(message)
                .status(status)
                .build();
    }

    public static <T> ApiResponse<T> success(T response, int status) {
        return ApiResponse.<T>builder()
                .data(response)
                .message("success")
                .status(status)
                .build();
    }

    public static <T> ApiResponse<T> error(T response, String message, int status) {
        return ApiResponse.<T>builder()
                .data(response)
                .message(message)
                .status(status)
                .build();
    }

    public static <T> ApiResponse<T> error(T response, int status) {
        return ApiResponse.<T>builder()
                .data(response)
                .message("error")
                .status(status)
                .build();
    }


}
