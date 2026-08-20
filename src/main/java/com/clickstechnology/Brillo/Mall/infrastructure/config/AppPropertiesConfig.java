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
        private String url = "https://graph.facebook.com/v25.0/112401001853611/messages";
        private String token = "EAAfLLVikCN0BR6eSOLf9fDDkA1kPCX5FRx8PwRyB9i3WzqkcOwj6YL6FtLpKF9DkI93Nj9C9auzOSXkAtqDYNz2ZCZAqbZAkKOTnLc13n9BtwwrZASZBGtT6bTisPeXZANQMS9DVugVp2tZATu12hxzZBXc3c9Ao4tK1RZCURhydNNuykpd0vHyxaTbLRL1ZCstE1WtfYlX0Y2033QP79ClWvNledmmOsQaRsnE1WYe20FwceEvoZASe6eKP78M975HZBkvHJzrmLuybybnt5AXZBoYEPfIfL9d9LX0nHigZDZD";
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
