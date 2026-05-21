package com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextMessage {

    @JsonProperty("preview_url")
    private Boolean previewUrl;

    private String body;
}