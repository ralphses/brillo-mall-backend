package com.clickstechnology.Brillo.Mall.application.dto;

import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String id;
    private String email;
    private String username;
    private String phoneNumber;
    private String fullName;
    private List<String> roles;
    private String referredBy;
    private boolean oauth2User = false;
    private EntityStatus status;
}
