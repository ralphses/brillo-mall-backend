package com.clickstechnology.Brillo.Mall.domain.user;

import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.InvitationDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.projections.AuthUser;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.UserStatus;
import com.clickstechnology.Brillo.Mall.application.exception.ResourceNotFoundException;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto findByEmailIgnoreCase(String email) {
        return findUserBy(() -> userRepository.findByEmailIgnoreCase(email))
                .dto();
    }

    @Override
    public AuthUser findAuthUserByEmail(String email) {
        return userRepository.findAuthUserByEmailOrPhone(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public AuthUser findAuthUserByPhone(String phoneNumber) {
        return findAuthUserByEmail(phoneNumber);
    }

    @Override
    public UserDto findByPhoneNumber(String phoneNumber) {
        return findUserBy(() -> userRepository.findByPhoneNumber(phoneNumber))
                .dto();
    }

    private User findUserBy(Supplier<Optional<User>> supplier) {
        return supplier.get()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }


    @Override
    public boolean exists(String username) {
        return userRepository.existsByUserName(username);
    }

    @Override
    public void registerNewUser(RegisterRequest request, InvitationDto invitedBy, boolean isOauth2User) {

        final String username = request.getEmailOrPhone();
        MessageMedium medium = AppUtils.resolveMessageMedium(username);

        User newUser = User.builder()
                .username(username)
                .fullName(request.getFullName())
                .password(request.getPassword())
                .oauth2User(isOauth2User)
                .email(medium == MessageMedium.EMAIL ? username : null)
                .phoneNumber(medium == MessageMedium.PHONE ? username : null)
                .invitedBy(invitedBy != null ? invitedBy.getInviteCode() : null)
                .status(UserStatus.PENDING)
                .roles(Collections.singletonList(UserRole.USER))
                .build();

        userRepository.save(newUser);
    }


    @Override
    public AuthUser findUserByUsername(String username) {
        return userRepository.findAuthUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public Optional<UserDto> getIncompleteUserByUser(String username) {
        return userRepository.findByUsernameIgnoreCaseAndStatus(
                username, UserStatus.PENDING
        ).map(User::dto);
    }

    @Override
    @Transactional
    public void updateUserDetails(RegisterRequest request, UserDto userDto) {
        userRepository.findByUsername(userDto.getUsername())
                .ifPresent(existingUser -> {
                    existingUser.setPassword(request.getPassword());
                    existingUser.setEmail(userDto.getEmail());
                    existingUser.setFullName(userDto.getFullName());
                    existingUser.setPhoneNumber(userDto.getPhoneNumber());;
                });
    }
}
