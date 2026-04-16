package com.clickstechnology.Brillo.Mall.application.dto.projections;

import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import lombok.Setter;

import java.util.List;

@Setter
public class CachedAuthUser implements AuthUser {

    private String phoneNumber;
    private String email;
    private String username;
    private String password;
    private EntityStatus status;
    private List<String> roles;

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public EntityStatus getStatus() {
        return status;
    }

    @Override
    public List<String> getRoles() {
        return roles;
    }
}
