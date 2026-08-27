package com.clickstechnology.Brillo.Mall.application.dto.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BusinessConversationDetailDto extends BusinessConversationSummaryDto {
    private List<MessageDto> recentMessages;
}
