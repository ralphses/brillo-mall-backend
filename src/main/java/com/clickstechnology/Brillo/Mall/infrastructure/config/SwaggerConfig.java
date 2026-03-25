package com.clickstechnology.Brillo.Mall.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SwaggerConfig for Brilo V1 APIs.
 *
 * Brilo is a product of Clicks Technology Limited.
 *
 * @author Eze.Raphael
 * @version 1.0.0
 * @since 1.0.0
 */

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI briloOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("BRILO API")
                        .description("""
                            Brilo is a WhatsApp-first business operating system for SMEs,
                            developed by Clicks Technology Limited.

                            It enables businesses to sell products and offer services via WhatsApp,
                            with an optional web-based ordering interface.

                            ## Core Capabilities
                            - Product sales via WhatsApp & Web
                            - Service requests & bookings
                            - Customer and conversation management
                            - Order & service tracking
                            - Payment handling (Online & Pay on Delivery)

                            ## WhatsApp Integration APIs
                            These endpoints handle:
                            - Incoming webhook messages from WhatsApp
                            - Conversation/session management (24-hour rule)
                            - Message processing and routing per business

                            ## Business (Dashboard) APIs
                            Used by businesses to:
                            - Manage products
                            - Manage services
                            - Track orders and bookings
                            - Manage customers

                            ## Web Ordering APIs
                            Public endpoints used for:
                            - Product browsing
                            - Service browsing
                            - Cart & checkout
                            - Booking requests

                            ## AI Support (Intent Classification)
                            - Used ONLY for intent detection
                            - No direct response generation
                            - Fallback to structured flows when unclear

                            ## Multi-Tenancy
                            - Each business operates in isolation
                            - Tenant resolution based on request context
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Clicks Technology Limited")
                                .email("support@clickstechnology.com")
                        )
                )

                // 🔐 JWT Security Scheme (for dashboard & protected APIs)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}