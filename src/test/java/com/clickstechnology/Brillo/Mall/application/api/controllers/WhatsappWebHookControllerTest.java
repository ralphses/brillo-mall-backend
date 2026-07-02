package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.api.contracts.WhatsappService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WhatsappWebHookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WhatsappService whatsappService;

    @Test
    @DisplayName("GET /api/v1/whatsapp/webhook verifies the challenge")
    void verifyWebhook_returnsChallenge() throws Exception {
        when(whatsappService.verifyWebhook("subscribe", "verify-token", "challenge-code"))
                .thenReturn("challenge-code");

        mockMvc.perform(get("/api/v1/whatsapp/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "verify-token")
                        .param("hub.challenge", "challenge-code"))
                .andExpect(status().isOk())
                .andExpect(content().string("challenge-code"));
    }

    @Test
    @DisplayName("POST /api/v1/whatsapp/webhook acknowledges inbound events immediately")
    void receiveWebhook_acknowledgesImmediately() throws Exception {
        ObjectNode payload = objectMapper.createObjectNode();
        doNothing().when(whatsappService).handleWebhook(any());

        mockMvc.perform(post("/api/v1/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }
}
