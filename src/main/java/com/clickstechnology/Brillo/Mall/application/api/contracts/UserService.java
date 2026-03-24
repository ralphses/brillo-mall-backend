package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.InvitationDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.projections.AuthUser;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;

import java.util.Optional;


public interface UserService {
    UserDto findByEmailIgnoreCase(String email);
    AuthUser findAuthUserByEmail(String email);
    AuthUser findAuthUserByPhone(String phoneNumber);

    UserDto findByPhoneNumber(String phoneNumber);

    boolean exists(String username);

    void registerNewUser(RegisterRequest request, InvitationDto invitedBy, boolean isOauth2User);

    AuthUser findUserByUsername(String username);

    Optional<UserDto> getIncompleteUserByUser(String username);

    void updateUserDetails(RegisterRequest request, UserDto userDto);

}
