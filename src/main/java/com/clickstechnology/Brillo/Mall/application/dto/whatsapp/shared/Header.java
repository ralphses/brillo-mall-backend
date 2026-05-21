package com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared;

import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Header {

    private WhatsappMessageType type;
    private String text;
    private Media image;
    private Media video;
    private Media document;
}