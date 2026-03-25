package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.InvitationDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;

public interface UserInviteService {
    InvitationDto processInvitation(String inviteCode, String username);

    InvitationDto fromUser(UserDto userDto);
}
