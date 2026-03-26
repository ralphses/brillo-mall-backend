package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.request.CreateOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.VerifyOtpRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.VerifyOtpResponse;
import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.OtpType;
import com.clickstechnology.Brillo.Mall.infrastructure.otp.OtpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class OtpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OtpService otpService;

    private CreateOtpRequest createOtpRequest;
    private VerifyOtpRequest verifyOtpRequest;

    @BeforeEach
    void setUp() {
        createOtpRequest = CreateOtpRequest.builder()
                .messageMedium(MessageMedium.EMAIL)
                .recipients(List.of("test@example.com"))
                .otpType(OtpType.PASSWORD_RESET)
                .message("Your OTP is 123456")
                .build();

        verifyOtpRequest = VerifyOtpRequest.builder()
                .otp("123456")
                .otpType(OtpType.PASSWORD_RESET)
                .username("test@example.com")
                .build();
    }

    @Test
    void createOtp_ShouldReturn201_WhenRequestIsValid() throws Exception {
        // Arrange
        doNothing().when(otpService).createOtp(any(), any());

        // Act & Assert
        mockMvc.perform(post("/api/v1/otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOtpRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Otp sent successfully"))
                .andExpect(jsonPath("$.status").value(201));
    }

    @Test
    void verifyOtp_ShouldReturn200_WhenOtpIsValid() throws Exception {
        // Arrange
        VerifyOtpResponse mockResponse = VerifyOtpResponse.builder()
                .verified(true)
                .message("OTP verified successfully")
                .build();

        when(otpService.verifyOtp(any(), any())).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verified").value(true))
                .andExpect(jsonPath("$.data.message").value("OTP verified successfully"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void verifyOtp_ShouldReturn200_WhenOtpIsInvalid() throws Exception {
        // Arrange
        VerifyOtpResponse mockResponse = VerifyOtpResponse.builder()
                .verified(false)
                .message("Invalid or expired OTP")
                .build();

        when(otpService.verifyOtp(any(), any())).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verified").value(false))
                .andExpect(jsonPath("$.data.message").value("Invalid or expired OTP"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void createOtp_ShouldReturn400_WhenRequestBodyIsMissing() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/otp")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createOtp_ShouldReturn500_WhenServiceThrowsException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Unexpected error")).when(otpService).createOtp(any(), any());

        // Act & Assert
        mockMvc.perform(post("/api/v1/otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOtpRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

}