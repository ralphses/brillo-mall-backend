package com.clickstechnology.Brillo.Mall.application.features.whatsapp;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.MessageSendService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsappResponse;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import com.clickstechnology.Brillo.Mall.application.enums.FlowSessionStatus;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappType;
import com.clickstechnology.Brillo.Mall.domain.conversation.ConversationRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.MessageRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.flow.ConversationFlowSessionRepository;
import com.clickstechnology.Brillo.Mall.infrastructure.caching.CacheUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class WhatsappServiceImplIntegrationTest {

    @Autowired
    private WhatsappServiceImpl whatsappService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationFlowSessionRepository flowSessionRepository;

    @MockitoBean
    private MessageSendService messageSendService;

    @MockitoBean
    private CacheUtil cacheUtil;

    private BusinessDto business;
    private int outboundCounter;

    @BeforeEach
    void setUp() {
        RegisterRequest registerRequest = new RegisterRequest(
                "WhatsApp Owner",
                "07000000001",
                "Password123",
                null
        );
        userService.registerNewUser(registerRequest, null, false);
        UserDto user = userService.findByUsername("07000000001");

        OnboardBusinessRequest request = new OnboardBusinessRequest();
        request.setBusinessName("Whatsapp Test Mart");
        businessService.createNew(request, user, "logo.png", BusinessCategory.PRODUCTS);
        business = businessService.findByBusinessSlug("whatsapp-test-mart");
        UpdateBusinessRequest updateBusinessRequest = new UpdateBusinessRequest();
        updateBusinessRequest.setWhatsappNumber("2348030000000");
        updateBusinessRequest.setWhatsappType(WhatsappType.DEDICATED);
        businessService.updateBusiness(business.getId(), updateBusinessRequest);
        business = businessService.findByBusinessId(business.getId());

        outboundCounter = 0;
        lenient().when(messageSendService.sendMessage(any())).thenAnswer(invocation -> new WhatsappResponse(
                "whatsapp",
                List.of(),
                List.of(new WhatsappResponse.ResponseMessage("wamid.outbound." + (++outboundCounter)))
        ));
    }

    @Test
    @DisplayName("Inbound greeting creates a session and persists both turns")
    void inboundGreeting_createsConversationAndOutboundMenu() throws Exception {
        JsonNode payload = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348030000000",
                              "phone_number_id": "123456"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Tunde" },
                                "wa_id": "2348011111111"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348011111111",
                                "id": "wamid.inbound.1",
                                "timestamp": "1719830400",
                                "type": "text",
                                "text": { "body": "Hi" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);

        whatsappService.processWebhookPayload(payload);

        var conversation = conversationRepository.findByWhatsappConversationId("2348011111111").orElseThrow();
        assertThat(conversation.getBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getStatus()).isEqualTo(ConversationStatus.AWAITING_USER);
        assertThat(conversation.getLastIntent()).isEqualTo("GREETING");
        assertThat(conversation.getActiveTaskKey()).isEqualTo("MENU");
        assertThat(messageRepository.findByWhatsappMessageId("wamid.inbound.1")).isPresent();
        assertThat(messageRepository.count()).isEqualTo(2);
        verify(messageSendService, times(1)).sendMessage(any());
    }

    @Test
    @DisplayName("Duplicate WhatsApp deliveries are deduped")
    void duplicateWebhook_isIgnored() throws Exception {
        JsonNode payload = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348030000000"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Ada" },
                                "wa_id": "2348012222222"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348012222222",
                                "id": "wamid.inbound.duplicate",
                                "timestamp": "1719830400",
                                "type": "text",
                                "text": { "body": "Hello" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);

        whatsappService.processWebhookPayload(payload);
        whatsappService.processWebhookPayload(payload);

        var conversation = conversationRepository.findByWhatsappConversationId("2348012222222").orElseThrow();
        assertThat(messageRepository.count()).isEqualTo(2);
        verify(messageSendService, times(1)).sendMessage(any());
    }

    @Test
    @DisplayName("Support request moves the conversation into human takeover")
    void supportRequest_entersHumanTakeover() throws Exception {
        JsonNode greeting = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348030000000"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Kemi" },
                                "wa_id": "2348013333333"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348013333333",
                                "id": "wamid.inbound.greeting",
                                "timestamp": "1719830400",
                                "type": "text",
                                "text": { "body": "Hi" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);
        JsonNode support = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348030000000"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Kemi" },
                                "wa_id": "2348013333333"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348013333333",
                                "id": "wamid.inbound.support",
                                "timestamp": "1719830500",
                                "type": "text",
                                "text": { "body": "I need a human agent" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);

        whatsappService.processWebhookPayload(greeting);
        whatsappService.processWebhookPayload(support);

        var conversation = conversationRepository.findByWhatsappConversationId("2348013333333").orElseThrow();
        assertThat(conversation.getStatus()).isEqualTo(ConversationStatus.HUMAN_TAKEOVER);
        assertThat(conversation.getHumanTakeover()).isTrue();
    }

    @Test
    @DisplayName("Business onboarding launches a WhatsApp Flow and persists flow session state")
    void onboardingLaunchesFlow() throws Exception {
        JsonNode greeting = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348030000000"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Flow User" },
                                "wa_id": "2348014444444"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348014444444",
                                "id": "wamid.inbound.flow.greeting",
                                "timestamp": "1719830400",
                                "type": "text",
                                "text": { "body": "Hi" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);
        JsonNode onboard = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348030000000"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Flow User" },
                                "wa_id": "2348014444444"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348014444444",
                                "id": "wamid.inbound.flow.onboard",
                                "timestamp": "1719830500",
                                "type": "interactive",
                                "interactive": {
                                  "type": "list_reply",
                                  "list_reply": {
                                    "id": "menu:onboard",
                                    "title": "Onboard your business"
                                  }
                                }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);

        whatsappService.processWebhookPayload(greeting);
        whatsappService.processWebhookPayload(onboard);

        var conversation = conversationRepository.findByWhatsappConversationId("2348014444444").orElseThrow();
        var flowSession = flowSessionRepository.findByConversation_Reference(conversation.getReference()).orElseThrow();

        assertThat(flowSession.getFlowStatus()).isEqualTo(FlowSessionStatus.LAUNCHED);
        assertThat(flowSession.getFlowId()).isEqualTo("brillo-business-onboarding");
        assertThat(flowSession.getLaunchTaskKey()).isEqualTo("BUSINESS_ONBOARDING_TASK");
        assertThat(flowSession.getLaunchStateKey()).isEqualTo("COLLECT_BUSINESS_TYPE");
        assertThat(flowSession.getFlowToken()).isNotBlank();
        assertThat(messageRepository.findByWhatsappMessageId("wamid.inbound.flow.onboard")).isPresent();
        verify(messageSendService, times(2)).sendMessage(any());
    }

    @Test
    @DisplayName("Flow submissions are parsed and persisted back into the conversation runtime")
    void flowSubmission_isPersisted() throws Exception {
        JsonNode greeting = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348030000000"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Flow User" },
                                "wa_id": "2348015555555"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348015555555",
                                "id": "wamid.inbound.flow2.greeting",
                                "timestamp": "1719830400",
                                "type": "text",
                                "text": { "body": "Hi" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);
        JsonNode onboard = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348030000000"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Flow User" },
                                "wa_id": "2348015555555"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348015555555",
                                "id": "wamid.inbound.flow2.onboard",
                                "timestamp": "1719830500",
                                "type": "interactive",
                                "interactive": {
                                  "type": "list_reply",
                                  "list_reply": {
                                    "id": "menu:onboard",
                                    "title": "Onboard your business"
                                  }
                                }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);
        JsonNode submission = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348030000000"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Flow User" },
                                "wa_id": "2348015555555"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348015555555",
                                "id": "wamid.inbound.flow2.submit",
                                "timestamp": "1719830600",
                                "type": "interactive",
                                "interactive": {
                                  "type": "nfm_reply",
                                  "nfm_reply": {
                                    "name": "flow",
                                    "response_json": "{\\"business_name\\":\\"Flow Mart\\",\\"business_type\\":\\"PRODUCTS\\",\\"category\\":\\"PHARMACY\\"}"
                                  }
                                }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);

        whatsappService.processWebhookPayload(greeting);
        whatsappService.processWebhookPayload(onboard);
        whatsappService.processWebhookPayload(submission);

        var conversation = conversationRepository.findByWhatsappConversationId("2348015555555").orElseThrow();
        var flowSession = flowSessionRepository.findByConversation_Reference(conversation.getReference()).orElseThrow();

        assertThat(flowSession.getFlowStatus()).isEqualTo(FlowSessionStatus.SUBMITTED);
        assertThat(flowSession.getSubmissionPayload()).contains("Flow Mart");
        assertThat(messageRepository.findByWhatsappMessageId("wamid.inbound.flow2.submit")).isPresent();
        verify(messageSendService, times(3)).sendMessage(any());
    }
}
