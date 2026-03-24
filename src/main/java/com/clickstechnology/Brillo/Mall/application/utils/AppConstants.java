package com.clickstechnology.Brillo.Mall.application.utils;

import java.security.SecureRandom;

public final class AppConstants {

    private AppConstants() {}

    public static String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static String PHONE_REGEX = "^(\\+?\\d{1,3})?0?\\d{10}$";

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

    public static final SecureRandom SECURE_RANDOM = new SecureRandom();

}
