package com.clickstechnology.Brillo.Mall.infrastructure.external;

import com.clickstechnology.Brillo.Mall.application.api.contracts.MessageSendService;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsAppMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsappResponse;
import com.clickstechnology.Brillo.Mall.application.exception.ApplicationException;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import com.clickstechnology.Brillo.Mall.infrastructure.config.AppPropertiesConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
class WhatsappMessageSendService implements MessageSendService {

    private final AppPropertiesConfig properties;
    private final RestTemplate restTemplate;

    @Override
    public WhatsappResponse sendMessage(final WhatsAppMessageRequest whatsAppMessageRequest) {
        try {
            log.debug(":::Sending WhatsApp message of type {}", whatsAppMessageRequest != null ? whatsAppMessageRequest.getType() : null);
            if (whatsAppMessageRequest != null) {
                whatsAppMessageRequest.setTo(AppUtils.normalizeWhatsappPhoneNumber(whatsAppMessageRequest.getTo()));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getWhatsapp().getToken());
            HttpEntity<WhatsAppMessageRequest> requestEntity = new HttpEntity<>(whatsAppMessageRequest, headers);
            log.info(":::WhatsApp request: {}", whatsAppMessageRequest);
            log.info(":::WhatsApp request entity : {}", requestEntity);

            ResponseEntity<WhatsappResponse> response = restTemplate.exchange(properties.getWhatsapp().getUrl(), HttpMethod.POST, requestEntity, WhatsappResponse.class);
            log.info(":::WhatsApp gateway returned status {}", response.getStatusCode());
            log.info(":::WhatsApp gateway response {}", response);

            if (response.getStatusCode().is2xxSuccessful()) {
                WhatsappResponse responseBody = response.getBody();
                log.info(":::WhatsApp message accepted by gateway");
                return responseBody;
            }
            log.info(":::WhatsApp gateway rejected message with status {}", response.getStatusCode());

            throw new ApplicationException("Error sending WhatsApp Message");

        } catch (Exception e) {
            log.error(":::Failed to send WhatsApp message", e);
            throw new ApplicationException("Error sending WhatsApp Message", e);
        }
    }
}
