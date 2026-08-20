package com.clickstechnology.Brillo.Mall.infrastructure.external;

import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.TextMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsappResponse;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.shared.TextMessage;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappMessageType;
import com.clickstechnology.Brillo.Mall.infrastructure.config.AppPropertiesConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhatsappMessageSendServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    void sendMessage_postsJsonWithRequiredWhatsappFields() {
        AppPropertiesConfig properties = new AppPropertiesConfig();
        properties.getWhatsapp().setUrl("https://graph.facebook.com/v25.0/123/messages");
        properties.getWhatsapp().setToken("test-token");

        WhatsappMessageSendService service = new WhatsappMessageSendService(properties, restTemplate);

        when(restTemplate.exchange(
                eq(properties.getWhatsapp().getUrl()),
                eq(HttpMethod.POST),
                org.mockito.ArgumentMatchers.any(HttpEntity.class),
                eq(WhatsappResponse.class)
        )).thenReturn(new ResponseEntity<>(new WhatsappResponse(), HttpStatus.OK));

        TextMessageRequest request = TextMessageRequest.builder()
                .to("07035002025")
                .text(TextMessage.builder()
                        .body("Your order ORD17831596903748630 has been created successfully.")
                        .previewUrl(false)
                        .build())
                .build();

        service.sendMessage(request);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq(properties.getWhatsapp().getUrl()),
                eq(HttpMethod.POST),
                captor.capture(),
                eq(WhatsappResponse.class)
        );

        HttpEntity<?> entity = captor.getValue();
        HttpHeaders headers = entity.getHeaders();

        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer test-token");
        assertThat(entity.getBody()).isInstanceOf(TextMessageRequest.class);

        TextMessageRequest body = (TextMessageRequest) Objects.requireNonNull(entity.getBody());
        assertThat(body.getTo()).isEqualTo("2347035002025");
        assertThat(body.getMessagingProduct()).isEqualTo("whatsapp");
        assertThat(body.getRecipientType()).isEqualTo("individual");
        assertThat(body.getType()).isEqualTo(WhatsappMessageType.TEXT);
        assertThat(body.getText().getBody()).contains("Your order");
    }
}
