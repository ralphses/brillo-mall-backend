package com.clickstechnology.Brillo.Mall.application.dto;

import com.clickstechnology.Brillo.Mall.application.enums.InvitationMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDto {
    private String inviteCode;
    private String invitedBy;
    private String invitee;
    private InvitationMessage invitationMessage;
    private boolean valid;
}
