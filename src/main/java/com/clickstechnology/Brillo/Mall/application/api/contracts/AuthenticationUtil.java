package com.clickstechnology.Brillo.Mall.application.api.contracts;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.List;

public interface AuthenticationUtil {
    void validateUserCurrentPassword(String password, String currentPassword);

    String encodePassword(String newPassword);

    String getAuthenticatedUsername(HttpServletRequest httpServletRequest);

    JwtEncoderParameters generateAccessToken(String subject, List<String> roles, Instant issuedAt, Instant expiresAt);

    void logoutUser(String token, Instant expiresAt);
}
