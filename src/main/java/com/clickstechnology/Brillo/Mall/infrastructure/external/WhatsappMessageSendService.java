package com.clickstechnology.Brillo.Mall.infrastructure.external;

import com.clickstechnology.Brillo.Mall.application.api.contracts.MessageSendService;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsAppMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsappResponse;
import com.clickstechnology.Brillo.Mall.application.exception.ApplicationException;
import com.clickstechnology.Brillo.Mall.infrastructure.config.AppPropertiesConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
class WhatsappMessageSendService implements MessageSendService {

    private final AppPropertiesConfig properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public WhatsappResponse sendMessage(final WhatsAppMessageRequest whatsAppMessageRequest) {
        try {
            log.debug("Sending WhatsApp message of type {}", whatsAppMessageRequest != null ? whatsAppMessageRequest.getType() : null);
            String requestBody = objectMapper.writeValueAsString(whatsAppMessageRequest);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(properties.getWhatsapp().getToken());
            HttpEntity<String> stringHttpEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<WhatsappResponse> response = restTemplate.exchange(properties.getWhatsapp().getUrl(), HttpMethod.POST, stringHttpEntity, WhatsappResponse.class);
            log.debug("WhatsApp gateway returned status {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                WhatsappResponse responseBody = response.getBody();
                log.debug("WhatsApp message accepted by gateway");
                return responseBody;
            }
            log.error("WhatsApp gateway rejected message with status {}", response.getStatusCode());

            throw new ApplicationException("Error sending WhatsApp Message");

        } catch (Exception e) {
            log.error("Failed to send WhatsApp message", e);
            throw new ApplicationException("Error sending WhatsApp Message", e);
        }
    }
}
