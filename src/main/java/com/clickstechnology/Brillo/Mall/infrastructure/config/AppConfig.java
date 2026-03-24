package com.clickstechnology.Brillo.Mall.infrastructure.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties("app")
public class AppConfig {

    private Jwt jwt = new Jwt();
    private Long otpDuration;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Jwt {
        private String jwtSecrete;
    }
}
