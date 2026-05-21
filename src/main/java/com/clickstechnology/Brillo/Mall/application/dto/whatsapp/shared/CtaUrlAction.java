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
public class CtaUrlAction {

    @Builder.Default
    private String name = "cta_url";

    private Parameters parameters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameters {

        @JsonProperty("display_text")
        private String displayText;

        private String url;
    }
}