package com.clickstechnology.Brillo.Mall.application.dto.business;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedConversationAttributionDto {
    private Long entryAttributedCustomersCount;
    private Long activeSharedConversationsCount;
}
