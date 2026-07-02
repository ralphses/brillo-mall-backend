package com.clickstechnology.Brillo.Mall.domain.business_service;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceRequestService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceRequestDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.PlaceBusinessServiceRequestPayload;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import com.clickstechnology.Brillo.Mall.application.enums.ServiceRequestStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
class BusinessServiceRequestServiceImpl implements BusinessServiceRequestService {
    private static final int MAX_NEGOTIATION_ATTEMPTS = 3;

    private final BusinessServiceRequestRepository businessServiceRequestRepository;

    @Override
    @Transactional
    public void create(
            final PlaceBusinessServiceRequestPayload request,
            final CustomerDto customer,
            final BusinessServiceDto businessService) {

        validateNegotiableService(businessService);

        BusinessServiceRequest newBusinessServiceRequest = BusinessServiceRequest.builder()
                .businessServiceId(businessService.getId())
                .businessId(businessService.getBusinessId())
                .initialPrice(resolveInitialPrice(businessService))
                .notes(request.getNotes())
                .customerId(customer.getId())
                .humanTakeover(Boolean.TRUE.equals(request.getHumanTakeover()))
                .lastOfferedPrice(request.getLastOfferedPrice())
                .whatsappConversationId(request.getWhatsappConversationId())
                .userId(customer.getUserId())
                .requestStatus(ServiceRequestStatus.NEGOTIATING)
                .status(EntityStatus.PENDING)
                .build();

        businessServiceRequestRepository.save(newBusinessServiceRequest);
    }

    private BigDecimal resolveInitialPrice(BusinessServiceDto businessService) {
        if (businessService.getBasePrice() == null) {
            throw new BusinessException("This business service cannot be requested without a base price.");
        }
        return businessService.getBasePrice();
    }

    private void validateNegotiableService(BusinessServiceDto businessService) {
        boolean canNegotiate = businessService.isNegotiable()
                && PricingType.NEGOTIABLE == businessService.getPricingType()
                && businessService.isActive()
                && businessService.getStatus() == EntityStatus.ACTIVE;

        if (!canNegotiate) {
            throw new BusinessException("This business service is not valid or not negotiable.");
        }
    }

    private BusinessServiceRequest findByRequestId(String requestId) {
        return businessServiceRequestRepository.findByReference(requestId)
                .orElseThrow(() -> new BusinessException("Business Service Request Not Found"));
    }

    @Override
    public BusinessServiceRequestDto findById(String requestId) {
        return findByRequestId(requestId).dto();
    }

    @Override
    @Transactional
    public BusinessServiceRequestDto updateRequest(
            final String requestId,
            final PlaceBusinessServiceRequestPayload request,
            final boolean businessActor) {

        BusinessServiceRequest businessServiceRequest = findByRequestId(requestId);
        ensureMutable(businessServiceRequest);

        if (businessActor) {
            applyBusinessUpdate(businessServiceRequest, request);
        } else {
            applyCustomerUpdate(businessServiceRequest, request);
        }

        if (businessServiceRequest.getAgreedPrice() != null
                && businessServiceRequest.getRequestStatus() == ServiceRequestStatus.NEGOTIATING) {
            businessServiceRequest.setRequestStatus(ServiceRequestStatus.AGREED);
        }

        if (businessServiceRequest.getNegotiationAttempts() != null
                && businessServiceRequest.getNegotiationAttempts() >= MAX_NEGOTIATION_ATTEMPTS
                && businessServiceRequest.getRequestStatus() == ServiceRequestStatus.NEGOTIATING) {
            businessServiceRequest.setRequestStatus(ServiceRequestStatus.REJECTED);
        }

        businessServiceRequestRepository.save(businessServiceRequest);
        return businessServiceRequest.dto();
    }

    private void applyCustomerUpdate(BusinessServiceRequest businessServiceRequest, PlaceBusinessServiceRequestPayload request) {
        if (request.getAgreedPrice() != null || request.getRequestStatus() != null || Boolean.TRUE.equals(request.getHumanTakeover())) {
            throw new BusinessException("Customers cannot override negotiation decisions.");
        }

        if (request.getLastOfferedPrice() != null) {
            businessServiceRequest.setLastOfferedPrice(request.getLastOfferedPrice());
            Integer negotiationAttempts = businessServiceRequest.getNegotiationAttempts() == null
                    ? 0
                    : businessServiceRequest.getNegotiationAttempts();
            businessServiceRequest.setNegotiationAttempts(negotiationAttempts + 1);
        }

        if (request.getNotes() != null) {
            businessServiceRequest.setNotes(request.getNotes());
        }

        if (request.getWhatsappConversationId() != null) {
            businessServiceRequest.setWhatsappConversationId(request.getWhatsappConversationId());
        }
    }

    private void applyBusinessUpdate(BusinessServiceRequest businessServiceRequest, PlaceBusinessServiceRequestPayload request) {
        if (request.getLastOfferedPrice() != null) {
            businessServiceRequest.setLastOfferedPrice(request.getLastOfferedPrice());
        }

        if (request.getNotes() != null) {
            businessServiceRequest.setNotes(request.getNotes());
        }

        if (request.getWhatsappConversationId() != null) {
            businessServiceRequest.setWhatsappConversationId(request.getWhatsappConversationId());
        }

        if (request.getHumanTakeover() != null) {
            businessServiceRequest.setHumanTakeover(request.getHumanTakeover());
        }

        if (request.getAgreedPrice() != null) {
            businessServiceRequest.setAgreedPrice(request.getAgreedPrice());
            businessServiceRequest.setRequestStatus(ServiceRequestStatus.AGREED);
        }

        if (request.getRequestStatus() != null) {
            switch (request.getRequestStatus()) {
                case REJECTED, EXPIRED, CANCELLED -> businessServiceRequest.setRequestStatus(request.getRequestStatus());
                case AGREED -> {
                    if (businessServiceRequest.getAgreedPrice() == null && businessServiceRequest.getLastOfferedPrice() != null) {
                        businessServiceRequest.setAgreedPrice(businessServiceRequest.getLastOfferedPrice());
                    }
                    if (businessServiceRequest.getAgreedPrice() == null) {
                        throw new BusinessException("An agreed price is required before the request can be accepted.");
                    }
                    businessServiceRequest.setRequestStatus(ServiceRequestStatus.AGREED);
                }
                case BOOKED -> throw new BusinessException("Use the booking endpoint to mark a service request as booked.");
                case NEGOTIATING -> businessServiceRequest.setRequestStatus(ServiceRequestStatus.NEGOTIATING);
            }
        }
    }

    private void ensureMutable(BusinessServiceRequest businessServiceRequest) {
        if (isTerminal(businessServiceRequest.getRequestStatus())) {
            throw new BusinessException("Business service request is invalid or already processed.");
        }
    }

    private boolean isTerminal(ServiceRequestStatus requestStatus) {
        return requestStatus == ServiceRequestStatus.REJECTED
                || requestStatus == ServiceRequestStatus.EXPIRED
                || requestStatus == ServiceRequestStatus.BOOKED
                || requestStatus == ServiceRequestStatus.CANCELLED;
    }

    @Override
    public PaginatedResponse<BusinessServiceRequestDto> listForBusiness(
            String businessId,
            String businessServiceId,
            ServiceRequestStatus requestStatus,
            Pageable pageable) {
        Page<BusinessServiceRequest> requestsPage;
        if (businessId != null && businessServiceId != null && requestStatus != null) {
            requestsPage = businessServiceRequestRepository.findByBusinessIdAndBusinessServiceIdAndRequestStatus(
                    businessId, businessServiceId, requestStatus, pageable);
        } else if (businessId != null && businessServiceId != null) {
            requestsPage = businessServiceRequestRepository.findByBusinessIdAndBusinessServiceId(
                    businessId, businessServiceId, pageable);
        } else if (businessId != null && requestStatus != null) {
            requestsPage = businessServiceRequestRepository.findByBusinessIdAndRequestStatus(
                    businessId, requestStatus, pageable);
        } else if (businessId != null) {
            requestsPage = businessServiceRequestRepository.findByBusinessId(businessId, pageable);
        } else if (businessServiceId != null && requestStatus != null) {
            requestsPage = businessServiceRequestRepository.findByBusinessServiceIdAndRequestStatus(
                    businessServiceId, requestStatus, pageable);
        } else if (businessServiceId != null) {
            requestsPage = businessServiceRequestRepository.findByBusinessServiceId(businessServiceId, pageable);
        } else {
            requestsPage = businessServiceRequestRepository.findAll(pageable);
        }

        return buildPaginatedResponse(requestsPage, pageable);
    }

    @Override
    public PaginatedResponse<BusinessServiceRequestDto> listForBusinesses(
            Set<String> businessIds,
            ServiceRequestStatus requestStatus,
            Pageable pageable) {
        Page<BusinessServiceRequest> requestsPage;
        if (requestStatus != null) {
            requestsPage = businessServiceRequestRepository.findByBusinessIdInAndRequestStatus(
                    businessIds, requestStatus, pageable);
        } else {
            requestsPage = businessServiceRequestRepository.findByBusinessIdIn(businessIds, pageable);
        }

        return buildPaginatedResponse(requestsPage, pageable);
    }

    @Override
    public PaginatedResponse<BusinessServiceRequestDto> listForBusinessService(
            String businessServiceId,
            ServiceRequestStatus requestStatus,
            Pageable pageable) {
        Page<BusinessServiceRequest> requestsPage;
        if (requestStatus != null) {
            requestsPage = businessServiceRequestRepository.findByBusinessServiceIdAndRequestStatus(
                    businessServiceId, requestStatus, pageable);
        } else {
            requestsPage = businessServiceRequestRepository.findByBusinessServiceId(businessServiceId, pageable);
        }

        return buildPaginatedResponse(requestsPage, pageable);
    }

    @Override
    public PaginatedResponse<BusinessServiceRequestDto> listForUser(
            String id,
            ServiceRequestStatus requestStatus,
            Pageable pageable) {
        Page<BusinessServiceRequest> requestsPage;
        if (requestStatus != null) {
            requestsPage = businessServiceRequestRepository.findByUserIdAndRequestStatus(id, requestStatus, pageable);
        } else {
            requestsPage = businessServiceRequestRepository.findByUserId(id, pageable);
        }

        return buildPaginatedResponse(requestsPage, pageable);
    }

    private PaginatedResponse<BusinessServiceRequestDto> buildPaginatedResponse(
            Page<BusinessServiceRequest> requestsPage,
            Pageable pageable) {
        List<BusinessServiceRequestDto> requests = requestsPage.stream()
                .map(BusinessServiceRequest::dto)
                .collect(Collectors.toList());

        return PaginatedResponse.<BusinessServiceRequestDto>builder()
                .page(pageable.getPageNumber() + 1)
                .perPage(pageable.getPageSize())
                .total((int) requestsPage.getTotalElements())
                .totalPages(requestsPage.getTotalPages())
                .hasNext(requestsPage.hasNext())
                .hasPrevious(requestsPage.hasPrevious())
                .items(requests)
                .build();
    }

    @Override
    @Transactional
    public void delete(BusinessServiceRequestDto request) {
        BusinessServiceRequest serviceRequest = findByRequestId(request.getId());
        serviceRequest.setStatus(EntityStatus.DELETED);
        serviceRequest.setRequestStatus(ServiceRequestStatus.CANCELLED);
        businessServiceRequestRepository.save(serviceRequest);
    }

    @Override
    @Transactional
    public void markBooked(String requestId) {
        BusinessServiceRequest serviceRequest = findByRequestId(requestId);
        if (serviceRequest.getRequestStatus() == ServiceRequestStatus.BOOKED) {
            return;
        }

        if (serviceRequest.getRequestStatus() != ServiceRequestStatus.AGREED) {
            throw new BusinessException("Service request must be agreed before booking.");
        }

        serviceRequest.setRequestStatus(ServiceRequestStatus.BOOKED);
        businessServiceRequestRepository.save(serviceRequest);
    }

    @Override
    public void ensureBelongsToUser(BusinessServiceRequestDto serviceRequest, String userId) {
        if (!serviceRequest.getUser().getId().equals(userId)) {
            throw new BusinessException("The selected service request does not belong to you.");
        }
    }

    @Override
    public void ensureBelongsToService(BusinessServiceRequestDto serviceRequest, String businessServiceId) {
        if (!serviceRequest.getBusinessService().getId().equals(businessServiceId)) {
            throw new BusinessException("The selected service request does not belong to the business service.");
        }
    }
}
