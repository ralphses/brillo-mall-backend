package com.clickstechnology.Brillo.Mall.application.dto.projections;

import com.clickstechnology.Brillo.Mall.application.enums.UserStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonDeserialize(as = CachedAuthUser.class)
public interface AuthUser {
    String getPhoneNumber();
    String getEmail();
    String getUsername();
    String getPassword();
    UserStatus getStatus();
    List<String> getRoles();
}
