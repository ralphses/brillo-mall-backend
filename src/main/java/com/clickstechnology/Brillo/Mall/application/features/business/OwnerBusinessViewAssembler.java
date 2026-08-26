package com.clickstechnology.Brillo.Mall.application.features.business;

import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.SharedConversationAttributionDto;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationMode;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import com.clickstechnology.Brillo.Mall.application.enums.WhatsappType;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import com.clickstechnology.Brillo.Mall.domain.conversation.ConversationRepository;
import com.clickstechnology.Brillo.Mall.infrastructure.config.AppPropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerBusinessViewAssembler {

    private static final List<ConversationMode> SHARED_CONVERSATION_MODES = List.of(
            ConversationMode.SHARED_MARKETPLACE,
            ConversationMode.SHARED_BUSINESS
    );

    private static final List<ConversationStatus> ACTIVE_SHARED_STATUSES = List.of(
            ConversationStatus.ACTIVE,
            ConversationStatus.AWAITING_USER,
            ConversationStatus.AWAITING_BUSINESS,
            ConversationStatus.HUMAN_TAKEOVER
    );

    private final AppPropertiesConfig appPropertiesConfig;
    private final ConversationRepository conversationRepository;

    public BusinessDto enrich(BusinessDto business) {
        if (business == null) {
            return null;
        }

        business.setStorefrontLink(buildStorefrontLink(business.getSlug()));
        business.setSharedWhatsappLink(buildSharedWhatsappLink(business.getSlug()));
        business.setWhatsappEntryMode(resolveWhatsappEntryMode(business));
        business.setDedicatedNumberReady(hasDedicatedNumber(business));
        business.setSharedWhatsappManagedByBrillo(Boolean.TRUE);
        business.setSharedConversationAttribution(buildSharedConversationAttribution(business.getId()));
        return business;
    }

    public List<BusinessDto> enrichAll(List<BusinessDto> businesses) {
        return businesses.stream()
                .map(this::enrich)
                .toList();
    }

    private SharedConversationAttributionDto buildSharedConversationAttribution(String businessId) {
        if (businessId == null || businessId.isBlank()) {
            return SharedConversationAttributionDto.builder()
                    .entryAttributedCustomersCount(0L)
                    .activeSharedConversationsCount(0L)
                    .build();
        }

        long attributedCustomers = conversationRepository.countDistinctWhatsappConversationIdByEntryBusinessIdAndConversationModeIn(
                businessId,
                SHARED_CONVERSATION_MODES
        );
        long activeSharedConversations = conversationRepository.countByActiveBusinessIdAndConversationModeInAndStatusIn(
                businessId,
                SHARED_CONVERSATION_MODES,
                ACTIVE_SHARED_STATUSES
        );

        return SharedConversationAttributionDto.builder()
                .entryAttributedCustomersCount(attributedCustomers)
                .activeSharedConversationsCount(activeSharedConversations)
                .build();
    }

    private String buildStorefrontLink(String businessSlug) {
        return stripTrailingSlash(appPropertiesConfig.getPublicBaseUrl()) + "/api/v1/public/businesses/slug/" + businessSlug;
    }

    private String buildSharedWhatsappLink(String businessSlug) {
        String message = "Hi, I'm interested in Brillo store " + businessSlug;
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String sharedNumber = AppUtils.normalizeWhatsappPhoneNumber(appPropertiesConfig.getWhatsapp().getSharedNumber());
        return "https://wa.me/" + sharedNumber + "?text=" + encoded;
    }

    private String resolveWhatsappEntryMode(BusinessDto business) {
        return hasDedicatedNumber(business) ? WhatsappType.DEDICATED.name() : WhatsappType.SHARED.name();
    }

    private boolean hasDedicatedNumber(BusinessDto business) {
        return business.getWhatsappType() == WhatsappType.DEDICATED
                && business.getWhatsappNumber() != null
                && !business.getWhatsappNumber().isBlank();
    }

    private String stripTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://brillo.example";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
