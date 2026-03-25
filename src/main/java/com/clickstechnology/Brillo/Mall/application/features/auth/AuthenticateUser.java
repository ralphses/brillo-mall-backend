package com.clickstechnology.Brillo.Mall.application.features.auth;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.dto.request.LoginRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.LoginResponse;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.infrastructure.authentication.JwtProvider;
import com.clickstechnology.Brillo.Mall.infrastructure.config.AppPropertiesConfig;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticateUser {

    private final AuthenticationManager authenticationManager;
    private final AppPropertiesConfig appPropertiesConfig;
    private final AuthenticationUtil authenticationUtil;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @LoggableRequest
    public LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmailOrPhone(),
                            request.getPassword()
                    )
            );

            String username = authentication.getName();

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            Instant now = Instant.now();
            long expiryMinutes = appPropertiesConfig.getJwt().getExpiryTime();
            Instant expiresAt = now.plus(expiryMinutes, ChronoUnit.MINUTES);

            JwtEncoderParameters jwtEncoderParameters = authenticationUtil.generateAccessToken(username, roles, now, expiresAt);
            String token = jwtEncoder.encode(jwtEncoderParameters).getTokenValue();

            long expiresIn = Duration.between(now, expiresAt).getSeconds();

            return new LoginResponse(token, expiresIn);
        } catch (BadCredentialsException e) {
            throw new BusinessException("Invalid username or password");
        }
    }

    @LoggableRequest
    public void logout(HttpServletRequest httpServletRequest) {

        String token = JwtProvider.extractToken(httpServletRequest);
        Jwt jwt = jwtDecoder.decode(token);

        authenticationUtil.logoutUser(token, jwt.getExpiresAt());
    }
}
