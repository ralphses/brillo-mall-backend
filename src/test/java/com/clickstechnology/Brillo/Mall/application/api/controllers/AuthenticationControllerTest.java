package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.request.*;
import com.clickstechnology.Brillo.Mall.application.dto.response.*;
import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.OtpType;
import com.clickstechnology.Brillo.Mall.application.features.auth.AuthenticateUser;
import com.clickstechnology.Brillo.Mall.application.features.auth.RegisterNewUser;
import com.clickstechnology.Brillo.Mall.application.features.auth.UpdatePassword;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterNewUser registerNewUser;

    @MockitoBean
    private UpdatePassword updatePassword;

    @MockitoBean
    private AuthenticateUser authenticateUser;

    private RegisterRequest registerRequest;
    private ResendOtpRequest resendOtpRequest;
    private VerifyOtpRequest verifyOtpRequest;
    private ResetPasswordRequest resetPasswordRequest;
    private NewPasswordRequest newPasswordRequest;
    private ChangePasswordRequest changePasswordRequest;
    private LoginRequest loginRequest;

    private String email;
    private String fullName;

    @BeforeEach
    void setUp() {
        email = "test@example.com";
        fullName = "Test User";
        registerRequest = new RegisterRequest(fullName, email, "Password123@", "2345");
        resendOtpRequest = new ResendOtpRequest(email);
        verifyOtpRequest = new VerifyOtpRequest("123456", OtpType.REGISTRATION, email);
        resetPasswordRequest = new ResetPasswordRequest(email);
        newPasswordRequest = new NewPasswordRequest("newPassword123@", "newPassword123@", email);
        changePasswordRequest = new ChangePasswordRequest("Password123@", "oldPassword@1", "oldPassword@1");
        loginRequest = new LoginRequest(email, "Password123@");
    }

    @Test
    void register_ShouldReturn201_WhenRequestIsValid() throws Exception {
        RegisterResponse response = new RegisterResponse(fullName, email, MessageMedium.EMAIL);

        when(registerNewUser.execute(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Profile creation initiated. Enter OTP to verify your account."));
    }

    @Test
    void registerResendOtp_ShouldReturn200_WhenRequestIsValid() throws Exception {
        OtpSentResponse response = new OtpSentResponse("OTP sent successfully");

        when(registerNewUser.resendOtp(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resendOtpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.message").value("OTP sent successfully"));
    }

    @Test
    void registerVerifyOtp_ShouldReturn200_WhenRequestIsValid() throws Exception {
        VerifyOtpResponse response = new VerifyOtpResponse(true, "OTP verified successfully");

        when(registerNewUser.verifyOtp(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.verified").value(true));
    }

    @Test
    void resetPassword_ShouldReturn200_WhenRequestIsValid() throws Exception {
        OtpSentResponse response = new OtpSentResponse("Password reset OTP sent");

        when(updatePassword.resetPassword(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.message").value("Password reset OTP sent"));
    }

    @Test
    void resetResendOtp_ShouldReturn200_WhenRequestIsValid() throws Exception {
        OtpSentResponse response = new OtpSentResponse("OTP resent successfully");

        when(updatePassword.resendOtp(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/reset-password/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resendOtpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.message").value("OTP resent successfully"));
    }

    @Test
    void resetPasswordVerifyOtp_ShouldReturn200_WhenRequestIsValid() throws Exception {
        VerifyOtpResponse response = new VerifyOtpResponse(true, "OTP verified");

        when(updatePassword.verifyOtp(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/reset-password/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.verified").value(true));
    }

    @Test
    void newPassword_ShouldReturn200_WhenRequestIsValid() throws Exception {
        NewPasswordResponse response = new NewPasswordResponse("Password updated successfully");

        when(updatePassword.newPassword(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/new-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.message").value("Password updated successfully"));
    }

    @Test
    void changePassword_ShouldReturn200_WhenRequestIsValid() throws Exception {
        NewPasswordResponse response = new NewPasswordResponse("Password changed successfully");

        when(updatePassword.changePassword(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.message").value("Password changed successfully"));
    }

    @Test
    void login_ShouldReturn200_WhenRequestIsValid() throws Exception {
        LoginResponse response = new LoginResponse("token-12345", 23333L, DashboardData.builder().build());

        when(authenticateUser.login(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.token").value("token-12345"));
    }

    @Test
    void logout_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }
}