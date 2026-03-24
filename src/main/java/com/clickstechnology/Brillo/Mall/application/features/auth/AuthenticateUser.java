package com.clickstechnology.Brillo.Mall.application.features.auth;

import com.clickstechnology.Brillo.Mall.application.dto.request.LoginRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticateUser {
    public LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {
        return null;
    }

    public void logout(HttpServletRequest httpServletRequest) {

    }
}
