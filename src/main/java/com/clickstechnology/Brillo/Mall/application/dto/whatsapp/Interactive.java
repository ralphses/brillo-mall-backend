package com.clickstechnology.Brillo.Mall.application.dto.whatsapp;

import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Body;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Footer;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.Header;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Interactive {
    private WhatsappMessageType type;
    private Header header;
    private Body body;
    private Footer footer;
}