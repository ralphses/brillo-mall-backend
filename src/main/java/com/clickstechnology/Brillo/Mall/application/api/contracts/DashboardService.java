package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.DashboardData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserService userService;
    private final AuthenticationUtil authenticationUtil;

    public DashboardData getDashboardData(Authentication authentication) {
        UserDto userDto = userService.findByUsername(authentication.getName());

        return DashboardData.builder()
                .user(userDto)
                .build();
    }
}
