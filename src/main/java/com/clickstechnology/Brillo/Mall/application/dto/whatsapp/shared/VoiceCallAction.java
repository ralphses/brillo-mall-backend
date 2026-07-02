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
public class VoiceCallAction {

    @Builder.Default
    private String name = "voice_call";

    private Parameters parameters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameters {

        @JsonProperty("display_text")
        private String displayText;

        @JsonProperty("ttl_minutes")
        private Integer ttlMinutes;

        private String payload;
    }
}
