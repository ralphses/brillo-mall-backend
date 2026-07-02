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
}
