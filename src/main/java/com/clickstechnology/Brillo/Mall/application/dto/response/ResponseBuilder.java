package com.clickstechnology.Brillo.Mall.application.dto.response;

public final class ResponseBuilder {
    private ResponseBuilder() {}

    public static <T> ResponseWrapper<T> success(T response) {
        return ResponseWrapper.<T>builder()
                .data(response)
                .message("success")
                .status(200)
                .build();
    }

    public static ResponseWrapper<String> success() {
        return ResponseWrapper.<String>builder()
                .data("Operation successful!")
                .message("success")
                .status(200)
                .build();
    }

    public static <T> ResponseWrapper<T> success(T response, String message) {
        return ResponseWrapper.<T>builder()
                .data(response)
                .message(message)
                .status(200)
                .build();
    }

    public static <T> ResponseWrapper<T> success(T response, String message, int status) {
        return ResponseWrapper.<T>builder()
                .data(response)
                .message(message)
                .status(status)
                .build();
    }

    public static <T> ResponseWrapper<T> success(T response, int status) {
        return ResponseWrapper.<T>builder()
                .data(response)
                .message("success")
                .status(status)
                .build();
    }

    public static <T> ResponseWrapper<T> error(T response, String message, int status) {
        return ResponseWrapper.<T>builder()
                .data(response)
                .message(message)
                .status(status)
                .build();
    }

    public static <T> ResponseWrapper<T> error(T response, int status) {
        return ResponseWrapper.<T>builder()
                .data(response)
                .message("error")
                .status(status)
                .build();
    }


}
