package com.clickstechnology.Brillo.Mall.application.dto.whatsapp;

import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.CtaUrlAction;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CtaUrlInteractive extends Interactive {

    private CtaUrlAction action;

    {
        setType(WhatsappMessageType.CTA_URL);
    }
}
