package com.clickstechnology.Brillo.Mall.infrastructure.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties("app")
public class AppPropertiesConfig {

    private Jwt jwt = new Jwt();
    private Whatsapp whatsapp = new Whatsapp();
    private OpenAi openAi = new OpenAi();
    private Long otpDuration;
    private String defaultBusinessLogoUrl;
    private String defaultProductImageUrl = "https://test.png";

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Jwt {
        private String jwtSecrete;
        private String issuer = "brillo";
        private Integer expiryTime = 10;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Whatsapp {
        private String url;
        private String token;
        private String verifyToken;
        private Integer sessionWindowHours = 24;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OpenAi {
        private Boolean enabled = Boolean.FALSE;
        private String apiKey;
        private String baseUrl = "https://api.openai.com/v1";
        private String model = "gpt-4o-mini";
        private Integer timeoutSeconds = 15;
        private Double intentConfidenceThreshold = 0.75;
        private Double slotConfidenceThreshold = 0.75;
        private Integer cacheTtlSeconds = 300;
    }
}
