package com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared;

import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Button {

    private WhatsappMessageType type;
    private Reply reply;
}