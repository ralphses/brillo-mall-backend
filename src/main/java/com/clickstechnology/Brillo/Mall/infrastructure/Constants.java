package com.clickstechnology.Brillo.Mall.infrastructure;

public final class Constants {

    private Constants() {
    }

    public static final int PASSWORD_STRENGTH = 11;

    public static final String[] WHITE_LIST_URL = {
            "/v3/api-docs/**",
            "/api/v1/auth/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/webjars/**",
            "/actuator/**",
    };
}