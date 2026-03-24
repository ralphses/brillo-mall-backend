package com.clickstechnology.Brillo.Mall.application.dto.projections;

import com.clickstechnology.Brillo.Mall.application.enums.UserStatus;

import java.util.List;

public interface AuthUser {
    String getPhoneNumber();
    String getEmail();
    String getUsername();
    String getPassword();
    UserStatus getStatus();
    List<String> getRoles();
}
