package com.clickstechnology.Brillo.Mall.infrastructure.authentication;

import com.clickstechnology.Brillo.Mall.application.exception.UnauthorizedUserException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtDecoder jwtDecoder;
    private final JwtEncoder jwtEncoder;

    public String getAuthenticatedUsername(HttpServletRequest httpServletRequest) {
        return null;
    }

    public Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }

    public Jwt encode(JwtEncoderParameters encoderParameters) {
        return jwtEncoder.encode(encoderParameters);
    }

    public static String extractToken(HttpServletRequest httpServletRequest) {
        String authHeader = httpServletRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedUserException();
        }

        return authHeader.substring(7);
    }
}
