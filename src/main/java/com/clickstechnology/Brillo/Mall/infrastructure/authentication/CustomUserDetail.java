package com.clickstechnology.Brillo.Mall.infrastructure.authentication;

import com.clickstechnology.Brillo.Mall.application.dto.projections.AuthUser;
import com.clickstechnology.Brillo.Mall.application.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

record CustomUserDetail(AuthUser user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE.equals(user.getStatus());
    }
}
