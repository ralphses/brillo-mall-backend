package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.InvitationDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.projections.AuthUser;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.enums.UserRole;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface UserService {
    UserDto findByEmailIgnoreCase(String email);
    AuthUser findAuthUserByEmail(String email);

    UserDto findByUsername(String username);

    AuthUser findAuthUserByPhone(String phoneNumber);

    UserDto findByPhoneNumber(String phoneNumber);

    boolean exists(String username);

    void registerNewUser(RegisterRequest request, InvitationDto invitedBy, boolean isOauth2User);

    AuthUser findUserByUsername(String username);

    UserDto findById(String userId);

    Optional<UserDto> getIncompleteUserByUser(String username);

    void updateUserDetails(RegisterRequest request, UserDto userDto);

    void completeUserRegistration(UserDto userDto);

    void changePassword(String username, String currentPassword, String newPassword);

    List<String> getUserRoles(String username);

    void addRoleToUser(String username, UserRole role);

    void ensureUserHasRoles(UserDto userDto, Set<UserRole> roles);
}
