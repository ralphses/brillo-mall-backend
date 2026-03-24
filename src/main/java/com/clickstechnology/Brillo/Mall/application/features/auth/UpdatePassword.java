package com.clickstechnology.Brillo.Mall.application.features.auth;

import com.clickstechnology.Brillo.Mall.application.dto.request.ChangePasswordRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.NewPasswordRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.ResetPasswordRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.NewPasswordResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResetPasswordResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdatePassword {
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request, HttpServletRequest httpServletRequest) {
        return null;
    }

    public NewPasswordResponse newPassword(NewPasswordRequest request, HttpServletRequest httpServletRequest) {
        return null;
    }

    public NewPasswordResponse changePassword(ChangePasswordRequest request, HttpServletRequest httpServletRequest) {
        return null;
    }
}
