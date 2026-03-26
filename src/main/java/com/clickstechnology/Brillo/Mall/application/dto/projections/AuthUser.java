package com.clickstechnology.Brillo.Mall.application.dto.projections;

import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonDeserialize(as = CachedAuthUser.class)
public interface AuthUser {
    String getPhoneNumber();
    String getEmail();
    String getUsername();
    String getPassword();
    EntityStatus getStatus();
    List<String> getRoles();
}
