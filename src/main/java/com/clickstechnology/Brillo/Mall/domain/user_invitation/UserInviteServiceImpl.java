package com.clickstechnology.Brillo.Mall.domain.user_invitation;

import com.clickstechnology.Brillo.Mall.application.api.contracts.UserInviteService;
import com.clickstechnology.Brillo.Mall.application.dto.InvitationDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class UserInviteServiceImpl implements UserInviteService {

    @Override
    public InvitationDto processInvitation(String inviteCode, String username) {
        return null;
    }

    @Override
    public InvitationDto fromUser(UserDto userDto) {
        return null;
    }
}
