package com.clickstechnology.Brillo.Mall.application.dto.response;

import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private String fullName;
    private String username;
    private MessageMedium messageMedium;

}
