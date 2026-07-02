package com.clickstechnology.Brillo.Mall.application.dto.whatsapp;

import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.ButtonAction;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ButtonInteractive extends Interactive {

    private ButtonAction action;

    {
        setType(WhatsappMessageType.BUTTON);
    }
}
