package com.clickstechnology.Brillo.Mall.application.dto.request;

import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.OtpType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOtpRequest {
    private MessageMedium messageMedium;
    private List<String> recipients;
    private OtpType otpType;
    private String message;
}
