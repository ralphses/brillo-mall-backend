package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.request.LoginRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.NewPasswordRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterResendOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterVerifyRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.ResetPasswordRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.LoginResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.NewPasswordResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.RegisterResendOtpResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.RegisterResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.RegisterVerifyResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.ResetPasswordResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

public interface AuthenticationService {

    RegisterResponse register(@Valid RegisterRequest request, HttpServletRequest httpServletRequest);

    RegisterVerifyResponse verifyRegister(@Valid RegisterVerifyRequest request, HttpServletRequest httpServletRequest);

    RegisterResendOtpResponse registerResendOtp(@Valid RegisterResendOtpRequest request, HttpServletRequest httpServletRequest);

    ResetPasswordResponse resetPassword(@Valid ResetPasswordRequest request, HttpServletRequest httpServletRequest);

    NewPasswordResponse newPassword(@Valid NewPasswordRequest request, HttpServletRequest httpServletRequest);

    LoginResponse login(@Valid LoginRequest request, HttpServletRequest httpServletRequest);

    void logout(HttpServletRequest httpServletRequest);
}
