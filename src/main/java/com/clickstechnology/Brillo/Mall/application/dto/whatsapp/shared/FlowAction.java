package com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlowAction {

    @Builder.Default
    private String name = "flow";

    private Parameters parameters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Parameters {

        @JsonProperty("flow_message_version")
        private String flowMessageVersion;

        @JsonProperty("flow_token")
        private String flowToken;

        @JsonProperty("flow_id")
        private String flowId;

        @JsonProperty("flow_cta")
        private String flowCta;

        @JsonProperty("mode")
        private String mode;

        @JsonProperty("flow_action")
        private String flowAction;

        @JsonProperty("flow_action_payload")
        private Map<String, Object> flowActionPayload;
    }
}
