package com.clickstechnology.Brillo.Mall.domain.user;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.InvitationDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.projections.AuthUser;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.enums.MessageMedium;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.UserRole;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.exception.ResourceNotFoundException;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheNames;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationUtil authenticationUtil;
    private final CacheUtil cacheUtil;

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
    public UserDto findByUsername(String username) {
        UserDto cachedUserDto = cacheUtil.get(CacheNames.USER_USER + username, UserDto.class);
        if (cachedUserDto == null) {
            cachedUserDto = userRepository.findByUsername(username)
                    .map(User::dto)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            cacheUtil.set(CacheNames.USER_USER + username, cachedUserDto, Duration.ofMinutes(30));
        }

        return cachedUserDto;
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
                .status(EntityStatus.PENDING)
                .roles(Collections.singletonList(UserRole.USER))
                .build();

        userRepository.save(newUser);
    }


    @Override
    public AuthUser findUserByUsername(String username) {

        final String cacheKey = CacheNames.USER_AUTH + "::" + username;

        AuthUser cachedUser = cacheUtil.get(cacheKey, AuthUser.class);

        if (cachedUser != null) {
            return cachedUser;
        }

        AuthUser user = userRepository.findAuthUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        cacheUtil.set(cacheKey, user, Duration.ofMinutes(20));

        return user;
    }

    @Override
    public Optional<UserDto> getIncompleteUserByUser(String username) {
        return userRepository.findByUsernameIgnoreCaseAndStatus(
                username, EntityStatus.PENDING
        ).map(User::dto);
    }

    @Override
    @Transactional
    public void updateUserDetails(RegisterRequest request, UserDto userDto) {
        getByUsername(userDto.getUsername())
                .ifPresent(existingUser -> {
                    existingUser.setPassword(request.getPassword());
                    existingUser.setEmail(userDto.getEmail());
                    existingUser.setFullName(userDto.getFullName());
                    existingUser.setPhoneNumber(userDto.getPhoneNumber());
                });
    }

    private Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void completeUserRegistration(UserDto user) {
        getByUsername(user.getUsername())
                .ifPresentOrElse(thisUser -> {
                            thisUser.setStatus(EntityStatus.ACTIVE);
                            userRepository.save(thisUser);
                        },
                        () -> {
                            throw new BusinessException("User not found");
                        });
    }

    @Override
    public void changePassword(String username, String currentPassword, String newPassword) {
        getByUsername(username).ifPresent(user -> {
            authenticationUtil.validateUserCurrentPassword(user.getPassword(), currentPassword);
            String encodedPassword = authenticationUtil.encodePassword(newPassword);
            user.setPassword(encodedPassword);
            userRepository.save(user);
        });
    }

    @Override
    public List<String> getUserRoles(String username) {

        User user = userRepository.fetchAllByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return user.getRoles() == null
                ? List.of()
                : user.getRoles().stream()
                .map(Enum::name)
                .toList();
    }
}
