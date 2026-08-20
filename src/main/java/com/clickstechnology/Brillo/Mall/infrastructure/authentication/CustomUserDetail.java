package com.clickstechnology.Brillo.Mall.infrastructure.authentication;

import com.clickstechnology.Brillo.Mall.application.dto.projections.AuthUser;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

record CustomUserDetail(AuthUser user, List<String> roles) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
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
        System.out.println("user = " + user.getStatus());
        return EntityStatus.ACTIVE.equals(user.getStatus());
    }
}
