package com.clickstechnology.Brillo.Mall.application.dto.conversation;

import com.clickstechnology.Brillo.Mall.application.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private String id;
    private String reference;
    private String content;
    private MessageType messageType;
    private String intent;
    private Instant createdAt;
}
