package com.clickstechnology.Brillo.Mall.application.features.whatsapp;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceRequestService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BookedBusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.MessageSendService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.OrderService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.ProductService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceRequestDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BookedServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.product.AddProductRequest;
import com.clickstechnology.Brillo.Mall.application.dto.product.ProductDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.RegisterRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.AddBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationMode;
import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.TextMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsappResponse;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import com.clickstechnology.Brillo.Mall.application.enums.FlowSessionStatus;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappType;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.WhatsAppMessageRequest;
import com.clickstechnology.Brillo.Mall.application.dto.whatsapp.InteractiveMessageRequest;
import com.clickstechnology.Brillo.Mall.domain.conversation.ConversationRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.MessageRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.flow.ConversationFlowSessionRepository;
import com.clickstechnology.Brillo.Mall.domain.conversation.task.ConversationTaskSessionRepository;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class WhatsappServiceImplIntegrationTest {

    private static final String DEDICATED_NUMBER = "2348030000000";
    private static final String SHARED_NUMBER = "2348039999999";

    @Autowired
    private WhatsappServiceImpl whatsappService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BusinessServiceService businessServiceService;

    @Autowired
    private BusinessServiceRequestService businessServiceRequestService;

    @Autowired
    private BookedBusinessServiceService bookedBusinessServiceService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationFlowSessionRepository flowSessionRepository;

    @Autowired
    private ConversationTaskSessionRepository taskSessionRepository;

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
        updateBusinessRequest.setWhatsappNumber(DEDICATED_NUMBER);
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

        var conversation = findConversation("2348011111111", DEDICATED_NUMBER, ConversationMode.DEDICATED_BUSINESS);
        assertThat(conversation.getBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getEntryBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getActiveBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getMarketplaceMode()).isFalse();
        assertThat(conversation.getConversationMode()).isEqualTo(ConversationMode.DEDICATED_BUSINESS);
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

        var conversation = findConversation("2348012222222", DEDICATED_NUMBER, ConversationMode.DEDICATED_BUSINESS);
        assertThat(messageRepository.count()).isEqualTo(2);
        verify(messageSendService, times(1)).sendMessage(any());
    }

    @Test
    @DisplayName("Shared WhatsApp entry slug resolves entry and active business context")
    void sharedEntrySlug_resolvesBusinessContext() throws Exception {
        UpdateBusinessRequest updateBusinessRequest = new UpdateBusinessRequest();
        updateBusinessRequest.setWhatsappNumber(null);
        updateBusinessRequest.setWhatsappType(WhatsappType.SHARED);
        businessService.updateBusiness(business.getId(), updateBusinessRequest);
        business = businessService.findByBusinessId(business.getId());

        JsonNode payload = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348039999999",
                              "phone_number_id": "shared-123"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Tolu" },
                                "wa_id": "2348014444444"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348014444444",
                                "id": "wamid.shared.slug",
                                "timestamp": "1719830400",
                                "type": "text",
                                "text": { "body": "Hi, I'm interested in Brillo store whatsapp-test-mart" }
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

        var conversation = findConversation("2348014444444", SHARED_NUMBER, ConversationMode.SHARED_BUSINESS);
        assertThat(conversation.getBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getEntryBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getActiveBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getEntrySlug()).isEqualTo("whatsapp-test-mart");
        assertThat(conversation.getMarketplaceMode()).isFalse();
        assertThat(conversation.getConversationMode()).isEqualTo(ConversationMode.SHARED_BUSINESS);
        assertThat(conversation.getLastIntent()).isEqualTo("SHARED_STORE_ENTRY");
        assertThat(conversation.getActiveTaskKey()).isEqualTo("MENU");
        ArgumentCaptor<WhatsAppMessageRequest> requestCaptor = ArgumentCaptor.forClass(WhatsAppMessageRequest.class);
        verify(messageSendService, atLeastOnce()).sendMessage(requestCaptor.capture());
        assertThat(requestCaptor.getAllValues())
                .filteredOn(InteractiveMessageRequest.class::isInstance)
                .map(InteractiveMessageRequest.class::cast)
                .extracting(request -> request.getInteractive().getHeader().getText())
                .contains("Brillo for Whatsapp Test Mart");
        assertThat(messageRepository.findByWhatsappMessageId("wamid.shared.slug")).isPresent();
        verify(messageSendService, times(1)).sendMessage(any());
    }

    @Test
    @DisplayName("Unresolved shared entry falls back to marketplace mode")
    void unresolvedSharedEntry_entersMarketplaceMode() throws Exception {
        UpdateBusinessRequest updateBusinessRequest = new UpdateBusinessRequest();
        updateBusinessRequest.setWhatsappNumber(null);
        updateBusinessRequest.setWhatsappType(WhatsappType.SHARED);
        businessService.updateBusiness(business.getId(), updateBusinessRequest);
        business = businessService.findByBusinessId(business.getId());

        JsonNode payload = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348039999999",
                              "phone_number_id": "shared-123"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Bola" },
                                "wa_id": "2348015555555"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348015555555",
                                "id": "wamid.shared.marketplace",
                                "timestamp": "1719830400",
                                "type": "text",
                                "text": { "body": "Hi there" }
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

        var conversation = findConversation("2348015555555", SHARED_NUMBER, ConversationMode.SHARED_MARKETPLACE);
        assertThat(conversation.getBusinessId()).isNull();
        assertThat(conversation.getEntryBusinessId()).isNull();
        assertThat(conversation.getActiveBusinessId()).isNull();
        assertThat(conversation.getEntrySlug()).isNull();
        assertThat(conversation.getMarketplaceMode()).isTrue();
        assertThat(conversation.getLastIntent()).isEqualTo("MARKETPLACE_ENTRY");
        assertThat(messageRepository.findByWhatsappMessageId("wamid.shared.marketplace")).isPresent();
        verify(messageSendService, times(1)).sendMessage(any());
    }

    @Test
    @DisplayName("Shared business conversations resume on the shared number without requiring a new slug")
    void sharedBusinessConversation_resumesOnSharedNumber() throws Exception {
        UpdateBusinessRequest updateBusinessRequest = new UpdateBusinessRequest();
        updateBusinessRequest.setWhatsappNumber(null);
        updateBusinessRequest.setWhatsappType(WhatsappType.SHARED);
        businessService.updateBusiness(business.getId(), updateBusinessRequest);
        business = businessService.findByBusinessId(business.getId());

        JsonNode entryPayload = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348039999999"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Mide" },
                                "wa_id": "2348016666666"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348016666666",
                                "id": "wamid.shared.entry",
                                "timestamp": "1719830400",
                                "type": "text",
                                "text": { "body": "Hi, I'm interested in Brillo store whatsapp-test-mart" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);
        JsonNode followUpPayload = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348039999999"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Mide" },
                                "wa_id": "2348016666666"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348016666666",
                                "id": "wamid.shared.followup",
                                "timestamp": "1719830500",
                                "type": "text",
                                "text": { "body": "I want to buy soap" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);

        whatsappService.processWebhookPayload(entryPayload);
        whatsappService.processWebhookPayload(followUpPayload);

        var conversation = findConversation("2348016666666", SHARED_NUMBER, ConversationMode.SHARED_BUSINESS);
        assertThat(conversation.getEntryBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getActiveBusinessId()).isEqualTo(business.getId());
        assertThat(conversationRepository.findAllByWhatsappConversationId("2348016666666")).hasSize(1);
        assertThat(messageRepository.findByWhatsappMessageId("wamid.shared.followup")).isPresent();
    }

    @Test
    @DisplayName("Marketplace selection keeps the shared marketplace conversation identity")
    void sharedMarketplaceSelection_keepsMarketplaceIdentity() throws Exception {
        UpdateBusinessRequest updateBusinessRequest = new UpdateBusinessRequest();
        updateBusinessRequest.setWhatsappNumber(null);
        updateBusinessRequest.setWhatsappType(WhatsappType.SHARED);
        businessService.updateBusiness(business.getId(), updateBusinessRequest);
        business = businessService.findByBusinessId(business.getId());

        JsonNode entryPayload = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348039999999"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Dami" },
                                "wa_id": "2348017777777"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348017777777",
                                "id": "wamid.shared.market.entry",
                                "timestamp": "1719830400",
                                "type": "text",
                                "text": { "body": "Hello there" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);
        JsonNode selectionPayload = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348039999999"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Dami" },
                                "wa_id": "2348017777777"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348017777777",
                                "id": "wamid.shared.market.select",
                                "timestamp": "1719830500",
                                "type": "interactive",
                                "interactive": {
                                  "type": "list_reply",
                                  "list_reply": {
                                    "id": "business:%s",
                                    "title": "Whatsapp Test Mart"
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
                """.formatted(business.getId()));

        whatsappService.processWebhookPayload(entryPayload);
        whatsappService.processWebhookPayload(selectionPayload);

        var conversation = findConversation("2348017777777", SHARED_NUMBER, ConversationMode.SHARED_MARKETPLACE);
        assertThat(conversation.getEntryBusinessId()).isNull();
        assertThat(conversation.getActiveBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getMarketplaceMode()).isTrue();
        assertThat(conversation.getLastIntent()).isEqualTo("SHARED_STORE_SWITCH");
        assertThat(conversationRepository.findAllByWhatsappConversationId("2348017777777")).hasSize(1);
    }

    @Test
    @DisplayName("Shared business customers can browse other stores without losing entry attribution")
    void sharedBusinessBrowseOtherStores_preservesEntryAttribution() throws Exception {
        UpdateBusinessRequest updateBusinessRequest = new UpdateBusinessRequest();
        updateBusinessRequest.setWhatsappNumber(null);
        updateBusinessRequest.setWhatsappType(WhatsappType.SHARED);
        businessService.updateBusiness(business.getId(), updateBusinessRequest);
        business = businessService.findByBusinessId(business.getId());

        JsonNode entryPayload = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348039999999"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Mira" },
                                "wa_id": "2348019999999"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348019999999",
                                "id": "wamid.shared.entry.phase3",
                                "timestamp": "1719830400",
                                "type": "text",
                                "text": { "body": "Hi, I'm interested in Brillo store whatsapp-test-mart" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);
        JsonNode browsePayload = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348039999999"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Mira" },
                                "wa_id": "2348019999999"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348019999999",
                                "id": "wamid.shared.browse.phase3",
                                "timestamp": "1719830500",
                                "type": "interactive",
                                "interactive": {
                                  "type": "list_reply",
                                  "list_reply": {
                                    "id": "marketplace:browse",
                                    "title": "Browse other stores"
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

        whatsappService.processWebhookPayload(entryPayload);
        whatsappService.processWebhookPayload(browsePayload);

        var conversation = findConversation("2348019999999", SHARED_NUMBER, ConversationMode.SHARED_BUSINESS);
        assertThat(conversation.getEntryBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getActiveBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getLastIntent()).isEqualTo("MARKETPLACE_BROWSE");
        ArgumentCaptor<WhatsAppMessageRequest> requestCaptor = ArgumentCaptor.forClass(WhatsAppMessageRequest.class);
        verify(messageSendService, atLeastOnce()).sendMessage(requestCaptor.capture());
        assertThat(requestCaptor.getAllValues())
                .filteredOn(InteractiveMessageRequest.class::isInstance)
                .map(InteractiveMessageRequest.class::cast)
                .extracting(request -> request.getInteractive().getHeader().getText())
                .contains("Welcome to Brillo Marketplace");
    }

    @Test
    @DisplayName("Shared product orders execute against the active store and tracking does not rewrite attribution")
    void sharedStoreSwitch_productOrderUsesActiveBusinessAndTrackingKeepsAttribution() throws Exception {
        UpdateBusinessRequest sharedUpdate = new UpdateBusinessRequest();
        sharedUpdate.setWhatsappNumber(null);
        sharedUpdate.setWhatsappType(WhatsappType.SHARED);
        businessService.updateBusiness(business.getId(), sharedUpdate);
        business = businessService.findByBusinessId(business.getId());

        ProductDto entryStoreProduct = createProduct(business.getId(), "Entry Store Soap", "ENTRY-SOAP");

        OnboardBusinessRequest secondBusinessRequest = new OnboardBusinessRequest();
        secondBusinessRequest.setBusinessName("Switch Store Mart");
        UserDto user = userService.findByUsername("07000000001");
        businessService.createNew(secondBusinessRequest, user, "logo-switch.png", BusinessCategory.PRODUCTS);
        BusinessDto switchedBusiness = businessService.findByBusinessSlug("switch-store-mart");
        UpdateBusinessRequest switchedBusinessUpdate = new UpdateBusinessRequest();
        switchedBusinessUpdate.setWhatsappNumber(null);
        switchedBusinessUpdate.setWhatsappType(WhatsappType.SHARED);
        businessService.updateBusiness(switchedBusiness.getId(), switchedBusinessUpdate);
        switchedBusiness = businessService.findByBusinessId(switchedBusiness.getId());
        ProductDto switchedStoreProduct = createProduct(switchedBusiness.getId(), "Switch Store Detergent", "SWITCH-DETERGENT");

        whatsappService.processWebhookPayload(textPayload("2348021234567", SHARED_NUMBER, "wamid.phase4.entry", "Hi, I'm interested in Brillo store whatsapp-test-mart"));
        whatsappService.processWebhookPayload(interactivePayload("2348021234567", SHARED_NUMBER, "wamid.phase4.browse", "marketplace:browse", "Browse other stores"));
        whatsappService.processWebhookPayload(interactivePayload("2348021234567", SHARED_NUMBER, "wamid.phase4.select.b", "business:" + switchedBusiness.getId(), switchedBusiness.getName()));
        whatsappService.processWebhookPayload(interactivePayload("2348021234567", SHARED_NUMBER, "wamid.phase4.buy", "menu:buy", "Buy something"));
        whatsappService.processWebhookPayload(textPayload("2348021234567", SHARED_NUMBER, "wamid.phase4.invalid.product", "product:" + entryStoreProduct.getId()));
        whatsappService.processWebhookPayload(textPayload("2348021234567", SHARED_NUMBER, "wamid.phase4.valid.product", "product:" + switchedStoreProduct.getId()));
        whatsappService.processWebhookPayload(textPayload("2348021234567", SHARED_NUMBER, "wamid.phase4.quantity", "2"));
        whatsappService.processWebhookPayload(textPayload("2348021234567", SHARED_NUMBER, "wamid.phase4.address", "Delivery address: 12 Allen Avenue, Ikeja"));
        whatsappService.processWebhookPayload(textPayload("2348021234567", SHARED_NUMBER, "wamid.phase4.payment", "pay on delivery"));

        var conversation = findConversation("2348021234567", SHARED_NUMBER, ConversationMode.SHARED_BUSINESS);
        assertThat(conversation.getEntryBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getActiveBusinessId()).isEqualTo(switchedBusiness.getId());
        var taskSession = taskSessionRepository.findByConversation_Reference(conversation.getReference()).orElseThrow();
        assertThat(taskSession.getCurrentStateKey()).isEqualTo("COMPLETED");
        assertThat(taskSession.getSlotsJson()).contains("created_order_id");

        var customerOrders = orderService.findAllByCustomerId(conversation.getCustomerId(), 1, 10).getItems();
        assertThat(customerOrders).hasSize(1);
        var createdOrder = customerOrders.getFirst();
        assertThat(createdOrder.getBusinessId()).isEqualTo(switchedBusiness.getId());
        assertThat(createdOrder.getItems()).hasSize(1);
        assertThat(createdOrder.getItems().getFirst().getProduct().getId()).isEqualTo(switchedStoreProduct.getId());

        whatsappService.processWebhookPayload(interactivePayload("2348021234567", SHARED_NUMBER, "wamid.phase4.browse.again", "marketplace:browse", "Browse other stores"));
        whatsappService.processWebhookPayload(interactivePayload("2348021234567", SHARED_NUMBER, "wamid.phase4.select.a", "business:" + business.getId(), business.getName()));
        whatsappService.processWebhookPayload(interactivePayload("2348021234567", SHARED_NUMBER, "wamid.phase4.track", "menu:track", "My orders & bookings"));
        whatsappService.processWebhookPayload(textPayload("2348021234567", SHARED_NUMBER, "wamid.phase4.track.order", "order:" + createdOrder.getId()));

        conversation = findConversation("2348021234567", SHARED_NUMBER, ConversationMode.SHARED_BUSINESS);
        assertThat(conversation.getEntryBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getActiveBusinessId()).isEqualTo(business.getId());

        String switchedStoreName = switchedBusiness.getName();
        String entryStoreName = business.getName();

        ArgumentCaptor<WhatsAppMessageRequest> requestCaptor = ArgumentCaptor.forClass(WhatsAppMessageRequest.class);
        verify(messageSendService, atLeastOnce()).sendMessage(requestCaptor.capture());
        assertThat(requestCaptor.getAllValues())
                .filteredOn(TextMessageRequest.class::isInstance)
                .map(TextMessageRequest.class::cast)
                .extracting(request -> request.getText().getBody())
                .anySatisfy(body -> {
                    assertThat(body).contains("That product does not belong to " + switchedStoreName);
                    assertThat(body).contains("Products from " + switchedStoreName);
                })
                .anySatisfy(body -> {
                    assertThat(body).contains("Order " + createdOrder.getId());
                    assertThat(body).contains(switchedStoreName);
                    assertThat(body).contains("currently browsing " + entryStoreName);
                });
    }

    @Test
    @DisplayName("Shared marketplace, shared business, and dedicated business conversations can coexist")
    void conversationModes_coexistForSameCustomer() throws Exception {
        OnboardBusinessRequest request = new OnboardBusinessRequest();
        request.setBusinessName("Marketplace Shared Store");
        UserDto user = userService.findByUsername("07000000001");
        businessService.createNew(request, user, "logo-2.png", BusinessCategory.PRODUCTS);
        BusinessDto sharedBusiness = businessService.findByBusinessSlug("marketplace-shared-store");
        UpdateBusinessRequest sharedUpdate = new UpdateBusinessRequest();
        sharedUpdate.setWhatsappNumber(null);
        sharedUpdate.setWhatsappType(WhatsappType.SHARED);
        businessService.updateBusiness(sharedBusiness.getId(), sharedUpdate);
        sharedBusiness = businessService.findByBusinessId(sharedBusiness.getId());

        JsonNode marketplacePayload = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348039999999"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Teni" },
                                "wa_id": "2348018888888"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348018888888",
                                "id": "wamid.shared.marketplace",
                                "timestamp": "1719830400",
                                "type": "text",
                                "text": { "body": "Hi there" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);
        JsonNode sharedBusinessPayload = objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "2348039999999"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Teni" },
                                "wa_id": "2348018888888"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348018888888",
                                "id": "wamid.shared.business",
                                "timestamp": "1719830500",
                                "type": "text",
                                "text": { "body": "Hi, I'm interested in Brillo store marketplace-shared-store" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);
        JsonNode dedicatedPayload = objectMapper.readTree("""
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
                                "profile": { "name": "Teni" },
                                "wa_id": "2348018888888"
                              }
                            ],
                            "messages": [
                              {
                                "from": "2348018888888",
                                "id": "wamid.dedicated.coexist",
                                "timestamp": "1719830600",
                                "type": "text",
                                "text": { "body": "Hello dedicated store" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """);

        whatsappService.processWebhookPayload(marketplacePayload);
        whatsappService.processWebhookPayload(sharedBusinessPayload);
        whatsappService.processWebhookPayload(dedicatedPayload);

        assertThat(findConversation("2348018888888", SHARED_NUMBER, ConversationMode.SHARED_MARKETPLACE).getEntryBusinessId()).isNull();
        assertThat(findConversation("2348018888888", SHARED_NUMBER, ConversationMode.SHARED_BUSINESS).getEntryBusinessId()).isEqualTo(sharedBusiness.getId());
        assertThat(findConversation("2348018888888", DEDICATED_NUMBER, ConversationMode.DEDICATED_BUSINESS).getEntryBusinessId()).isEqualTo(business.getId());
        assertThat(conversationRepository.findAllByWhatsappConversationId("2348018888888")).hasSize(3);
    }

    @Test
    @DisplayName("Shared marketplace service booking uses the active business")
    void sharedMarketplace_serviceBookingUsesActiveBusiness() throws Exception {
        UpdateBusinessRequest sharedUpdate = new UpdateBusinessRequest();
        sharedUpdate.setWhatsappNumber(null);
        sharedUpdate.setWhatsappType(WhatsappType.SHARED);
        businessService.updateBusiness(business.getId(), sharedUpdate);
        business = businessService.findByBusinessId(business.getId());

        BusinessServiceDto fixedService = createService(
                business.getId(),
                "Home Cleaning",
                PricingType.FIXED,
                false,
                false,
                BigDecimal.valueOf(25000)
        );

        whatsappService.processWebhookPayload(textPayload("2348025551111", SHARED_NUMBER, "wamid.phase5.market.entry", "Hi there"));
        whatsappService.processWebhookPayload(interactivePayload("2348025551111", SHARED_NUMBER, "wamid.phase5.market.select", "business:" + business.getId(), business.getName()));
        whatsappService.processWebhookPayload(interactivePayload("2348025551111", SHARED_NUMBER, "wamid.phase5.market.services", "menu:services", "Book a service"));
        whatsappService.processWebhookPayload(textPayload("2348025551111", SHARED_NUMBER, "wamid.phase5.market.service", "book service:" + fixedService.getId()));
        whatsappService.processWebhookPayload(textPayload("2348025551111", SHARED_NUMBER, "wamid.phase5.market.location", "15 Admiralty Way, Lekki"));

        var conversation = findConversation("2348025551111", SHARED_NUMBER, ConversationMode.SHARED_MARKETPLACE);
        assertThat(conversation.getEntryBusinessId()).isNull();
        assertThat(conversation.getActiveBusinessId()).isEqualTo(business.getId());

        var taskSession = taskSessionRepository.findByConversation_Reference(conversation.getReference()).orElseThrow();
        assertThat(taskSession.getSlotsJson()).contains("booking_id");

        String bookingId = objectMapper.readTree(taskSession.getSlotsJson()).get("booking_id").asText();
        BookedServiceDto booking = bookedBusinessServiceService.findById(bookingId);
        assertThat(booking.getBusiness().getId()).isEqualTo(business.getId());
        assertThat(booking.getBusinessService().getId()).isEqualTo(fixedService.getId());
        assertThat(booking.getLocation()).isEqualTo("15 Admiralty Way, Lekki");

        ArgumentCaptor<WhatsAppMessageRequest> requestCaptor = ArgumentCaptor.forClass(WhatsAppMessageRequest.class);
        verify(messageSendService, atLeastOnce()).sendMessage(requestCaptor.capture());
        assertThat(requestCaptor.getAllValues())
                .filteredOn(TextMessageRequest.class::isInstance)
                .map(TextMessageRequest.class::cast)
                .extracting(request -> request.getText().getBody())
                .anySatisfy(body -> {
                    assertThat(body).contains("Booking " + bookingId);
                    assertThat(body).contains(business.getName());
                });
    }

    @Test
    @DisplayName("Shared service requests use active business and tracking keeps attribution")
    void sharedStoreSwitch_serviceRequestUsesActiveBusinessAndTrackingKeepsAttribution() throws Exception {
        UpdateBusinessRequest sharedUpdate = new UpdateBusinessRequest();
        sharedUpdate.setWhatsappNumber(null);
        sharedUpdate.setWhatsappType(WhatsappType.SHARED);
        businessService.updateBusiness(business.getId(), sharedUpdate);
        business = businessService.findByBusinessId(business.getId());

        BusinessServiceDto entryStoreService = createService(
                business.getId(),
                "Entry Store Repairs",
                PricingType.FIXED,
                false,
                false,
                BigDecimal.valueOf(18000)
        );

        OnboardBusinessRequest secondBusinessRequest = new OnboardBusinessRequest();
        secondBusinessRequest.setBusinessName("Switch Service Hub");
        UserDto user = userService.findByUsername("07000000001");
        businessService.createNew(secondBusinessRequest, user, "logo-service.png", BusinessCategory.PRODUCTS);
        BusinessDto switchedBusiness = businessService.findByBusinessSlug("switch-service-hub");
        UpdateBusinessRequest switchedBusinessUpdate = new UpdateBusinessRequest();
        switchedBusinessUpdate.setWhatsappNumber(null);
        switchedBusinessUpdate.setWhatsappType(WhatsappType.SHARED);
        businessService.updateBusiness(switchedBusiness.getId(), switchedBusinessUpdate);
        switchedBusiness = businessService.findByBusinessId(switchedBusiness.getId());
        BusinessServiceDto switchedStoreService = createService(
                switchedBusiness.getId(),
                "Switch Store Installation",
                PricingType.NEGOTIABLE,
                true,
                false,
                BigDecimal.valueOf(30000)
        );

        whatsappService.processWebhookPayload(textPayload("2348026662222", SHARED_NUMBER, "wamid.phase5.entry", "Hi, I'm interested in Brillo store whatsapp-test-mart"));
        whatsappService.processWebhookPayload(interactivePayload("2348026662222", SHARED_NUMBER, "wamid.phase5.browse", "marketplace:browse", "Browse other stores"));
        whatsappService.processWebhookPayload(interactivePayload("2348026662222", SHARED_NUMBER, "wamid.phase5.select.b", "business:" + switchedBusiness.getId(), switchedBusiness.getName()));
        whatsappService.processWebhookPayload(interactivePayload("2348026662222", SHARED_NUMBER, "wamid.phase5.services", "menu:services", "Book a service"));
        whatsappService.processWebhookPayload(textPayload("2348026662222", SHARED_NUMBER, "wamid.phase5.invalid.service", "book service:" + entryStoreService.getId()));
        whatsappService.processWebhookPayload(textPayload("2348026662222", SHARED_NUMBER, "wamid.phase5.valid.service", "book service:" + switchedStoreService.getId()));
        whatsappService.processWebhookPayload(textPayload("2348026662222", SHARED_NUMBER, "wamid.phase5.location", "22 Toyin Street, Ikeja"));

        var conversation = findConversation("2348026662222", SHARED_NUMBER, ConversationMode.SHARED_BUSINESS);
        assertThat(conversation.getEntryBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getActiveBusinessId()).isEqualTo(switchedBusiness.getId());

        var taskSession = taskSessionRepository.findByConversation_Reference(conversation.getReference()).orElseThrow();
        assertThat(taskSession.getCurrentStateKey()).isEqualTo("NEGOTIATION");
        assertThat(taskSession.getSlotsJson()).contains("service_request_id");

        String requestId = objectMapper.readTree(taskSession.getSlotsJson()).get("service_request_id").asText();
        BusinessServiceRequestDto serviceRequest = businessServiceRequestService.findById(requestId);
        assertThat(serviceRequest.getBusiness().getId()).isEqualTo(switchedBusiness.getId());
        assertThat(serviceRequest.getBusinessService().getId()).isEqualTo(switchedStoreService.getId());
        assertThat(serviceRequest.getRequestStatus().name()).isEqualTo("NEGOTIATING");

        whatsappService.processWebhookPayload(interactivePayload("2348026662222", SHARED_NUMBER, "wamid.phase5.browse.again", "marketplace:browse", "Browse other stores"));
        whatsappService.processWebhookPayload(interactivePayload("2348026662222", SHARED_NUMBER, "wamid.phase5.select.a", "business:" + business.getId(), business.getName()));
        whatsappService.processWebhookPayload(interactivePayload("2348026662222", SHARED_NUMBER, "wamid.phase5.track", "menu:track", "My orders & bookings"));
        whatsappService.processWebhookPayload(textPayload("2348026662222", SHARED_NUMBER, "wamid.phase5.track.request", "request:" + requestId));

        conversation = findConversation("2348026662222", SHARED_NUMBER, ConversationMode.SHARED_BUSINESS);
        assertThat(conversation.getEntryBusinessId()).isEqualTo(business.getId());
        assertThat(conversation.getActiveBusinessId()).isEqualTo(business.getId());

        String switchedStoreName = switchedBusiness.getName();
        String entryStoreName = business.getName();

        ArgumentCaptor<WhatsAppMessageRequest> requestCaptor = ArgumentCaptor.forClass(WhatsAppMessageRequest.class);
        verify(messageSendService, atLeastOnce()).sendMessage(requestCaptor.capture());
        assertThat(requestCaptor.getAllValues())
                .filteredOn(TextMessageRequest.class::isInstance)
                .map(TextMessageRequest.class::cast)
                .extracting(request -> request.getText().getBody())
                .anySatisfy(body -> {
                    assertThat(body).contains("That service does not belong to " + switchedStoreName);
                    assertThat(body).contains("Services from " + switchedStoreName);
                })
                .anySatisfy(body -> {
                    assertThat(body).contains("Service request " + requestId);
                    assertThat(body).contains(switchedStoreName);
                    assertThat(body).contains("currently browsing " + entryStoreName);
                });
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

        var conversation = findConversation("2348013333333", DEDICATED_NUMBER, ConversationMode.DEDICATED_BUSINESS);
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

        var conversation = findConversation("2348014444444", DEDICATED_NUMBER, ConversationMode.DEDICATED_BUSINESS);
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

        var conversation = findConversation("2348015555555", DEDICATED_NUMBER, ConversationMode.DEDICATED_BUSINESS);
        var flowSession = flowSessionRepository.findByConversation_Reference(conversation.getReference()).orElseThrow();

        assertThat(flowSession.getFlowStatus()).isEqualTo(FlowSessionStatus.SUBMITTED);
        assertThat(flowSession.getSubmissionPayload()).contains("Flow Mart");
        assertThat(messageRepository.findByWhatsappMessageId("wamid.inbound.flow2.submit")).isPresent();
        verify(messageSendService, times(3)).sendMessage(any());
    }

    private com.clickstechnology.Brillo.Mall.domain.conversation.Conversation findConversation(
            String whatsappId,
            String channelKey,
            ConversationMode conversationMode) {
        return conversationRepository.findByWhatsappConversationIdAndChannelKeyAndConversationMode(
                whatsappId,
                channelKey,
                conversationMode
        ).orElseThrow();
    }

    private JsonNode textPayload(String waId, String businessPhoneNumber, String messageId, String body) throws Exception {
        return objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "%s"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Phase Four User" },
                                "wa_id": "%s"
                              }
                            ],
                            "messages": [
                              {
                                "from": "%s",
                                "id": "%s",
                                "timestamp": "1719830400",
                                "type": "text",
                                "text": { "body": "%s" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """.formatted(businessPhoneNumber, waId, waId, messageId, body));
    }

    private JsonNode interactivePayload(String waId, String businessPhoneNumber, String messageId, String replyId, String title) throws Exception {
        return objectMapper.readTree("""
                {
                  "entry": [
                    {
                      "changes": [
                        {
                          "value": {
                            "metadata": {
                              "display_phone_number": "%s"
                            },
                            "contacts": [
                              {
                                "profile": { "name": "Phase Four User" },
                                "wa_id": "%s"
                              }
                            ],
                            "messages": [
                              {
                                "from": "%s",
                                "id": "%s",
                                "timestamp": "1719830500",
                                "type": "interactive",
                                "interactive": {
                                  "type": "list_reply",
                                  "list_reply": {
                                    "id": "%s",
                                    "title": "%s"
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
                """.formatted(businessPhoneNumber, waId, waId, messageId, replyId, title));
    }

    private ProductDto createProduct(String businessId, String productName, String sku) {
        AddProductRequest request = new AddProductRequest();
        request.setName(productName);
        request.setCategory("PHARMACY");
        request.setDescription(productName + " description");
        request.setPrice(BigDecimal.valueOf(1500));
        request.setDiscountedPrice(BigDecimal.valueOf(1200));
        request.setSku(sku);
        request.setFlashSale(false);
        request.setQuantity(25);
        return productService.createProduct(businessId, request, "product.png");
    }

    private BusinessServiceDto createService(
            String businessId,
            String serviceName,
            PricingType pricingType,
            boolean negotiable,
            boolean requiresSchedule,
            BigDecimal basePrice) {
        AddBusinessServiceRequest request = new AddBusinessServiceRequest();
        request.setBusinessId(businessId);
        request.setName(serviceName);
        request.setCategory("Beauty");
        request.setDescription(serviceName + " description");
        request.setPricingType(pricingType);
        request.setBasePrice(basePrice);
        request.setDurationMinutes(90);
        request.setNegotiable(negotiable);
        request.setRequiresSchedule(requiresSchedule);
        return businessServiceService.addService(request, userService.findByUsername("07000000001"));
    }
}
