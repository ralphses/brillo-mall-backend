package com.clickstechnology.Brillo.Mall.application.utils;

import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public final class AppUtils {
    private AppUtils() {}

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");


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

    public static void validatePasswordChange(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new BusinessException("New password must match confirm password.");
        }
    }

    public static BusinessCategory validateBusinessCategory(String businessCategory) {
        try {
            if (businessCategory == null || businessCategory.isBlank()) {
                throw new BusinessException("Business category cannot be null or empty");
            }
            return BusinessCategory.valueOf(businessCategory.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid business category");
        }
    }

    public static Pageable getPageable(Integer page, Integer pageSize) {
        return PageRequest.of(Math.max(0, page - 1), pageSize);
    }

    public static String generateSlug(@NotBlank String name) {
        String noWhitespace = WHITESPACE.matcher(name).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    public static String generateUniqueReference() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}