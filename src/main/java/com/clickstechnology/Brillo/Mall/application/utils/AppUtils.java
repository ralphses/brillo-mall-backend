package com.clickstechnology.Brillo.Mall.application.utils;

import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;

public final class AppUtils {
    private AppUtils() {}


    public static MessageMedium resolveMessageMedium(String username) {
        try {
            if (username == null || username.isBlank()) {
                throw new BusinessException("Username cannot be null or empty");
            }

            String trimmed = username.trim();

            if (trimmed.matches(AppConstants.EMAIL_REGEX)) {
                return MessageMedium.EMAIL;
            }

            if (trimmed.matches(AppConstants.PHONE_REGEX)) {
                return MessageMedium.PHONE;
            }

            throw new BusinessException("Invalid username format");
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid username");
        }
    }

}
