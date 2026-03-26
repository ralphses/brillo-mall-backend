package com.clickstechnology.Brillo.Mall.infrastructure.authentication;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.exception.UnauthorizedUserException;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheNames;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import com.clickstechnology.Brillo.Mall.infrastructure.config.AppPropertiesConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultAuthenticationUtil implements AuthenticationUtil {

    private final PasswordEncoder passwordEncoder;
    private final AppPropertiesConfig appPropertiesConfig;
    private final CacheUtil cacheUtil;

    @Override
    public void validateUserCurrentPassword(String encodedPassword, String providedPassword) {
        if (!passwordEncoder.matches(providedPassword, encodedPassword)) {
            throw new BadCredentialsException("Current password does not match the provided password");
        }
    }

    @Override
    public String encodePassword(String newPassword) {
        return passwordEncoder.encode(newPassword);
    }

    @Override
    public String getAuthenticatedUsername(HttpServletRequest httpServletRequest) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (!authentication.isAuthenticated()) {
            throw new UnauthorizedUserException();
        }
        String authenticationName = authentication.getName();
        return "anonymousUser".equals(authenticationName) ? null : authenticationName;
    }


    @Override
    public JwtEncoderParameters generateAccessToken(String subject, List<String> roles, Instant issuedAt, Instant expiresAt) {
        var claims = JwtClaimsSet.builder()
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(subject)
                .issuer(appPropertiesConfig.getJwt().getIssuer())
                .claim("roles", roles)
                .id(UUID.randomUUID().toString().replace("-", ""))
                .build();

        return JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS512).build(), claims);
    }

    @Override
    public void logoutUser(String token, Instant expiresAt) {

        Duration duration = Duration.between(Instant.now(), expiresAt);

        if (duration.getSeconds() > 0) {
            cacheUtil.set(
                    CacheNames.BLACKLISTED_TOKENS + token,
                    "revoked",
                    duration
            );
        }
    }
}
