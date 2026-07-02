package com.clickstechnology.Brillo.Mall.domain.business_service;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceRequestService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceRequestDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.PlaceBusinessServiceRequestPayload;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.ServiceRequestStatus;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
class BusinessServiceRequestServiceImpl implements BusinessServiceRequestService {
    private final BusinessServiceRequestRepository businessServiceRequestRepository;

    @Async
    @Override
    public void create(
            final PlaceBusinessServiceRequestPayload request,
            final CustomerDto customer,
            final BusinessServiceDto businessService) {

        // Ensure business
        BusinessServiceRequest newBusinessServiceRequest = BusinessServiceRequest.builder()
                .businessServiceId(businessService.getId())
                .businessId(businessService.getBusinessId())
                .agreedPrice(request.getAgreedPrice())
                .notes(request.getNotes())
                .customerId(customer.getId())
                .humanTakeover(request.getHumanTakeover())
                .initialPrice(request.getInitialPrice())
                .agreedPrice(request.getAgreedPrice())
                .lastOfferedPrice(request.getLastOfferedPrice())
                .whatsappConversationId(request.getWhatsappConversationId())
                .userId(customer.getUserId())
                .requestStatus(ServiceRequestStatus.NEGOTIATING)
                .build();

        businessServiceRequestRepository.save(newBusinessServiceRequest);

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
            final boolean updateNegotiationCounter) {

        BusinessServiceRequest businessServiceRequest = findByRequestId(requestId);

        if (request.getAgreedPrice() != null) {
            businessServiceRequest.setAgreedPrice(request.getAgreedPrice());
            businessServiceRequest.setRequestStatus(ServiceRequestStatus.AGREED);
        }
        if (request.getHumanTakeover() != null) {
            businessServiceRequest.setHumanTakeover(request.getHumanTakeover());
        }

        if (request.getInitialPrice() != null) {
            businessServiceRequest.setInitialPrice(request.getInitialPrice());
        }

        if (request.getNotes() != null) {
            businessServiceRequest.setNotes(request.getNotes());
        }

        if (request.getWhatsappConversationId() != null) {
            businessServiceRequest.setWhatsappConversationId(request.getWhatsappConversationId());
        }

        if (request.getNegotiationAttemptsCount() != null) {
            if (updateNegotiationCounter) {
                Integer negotiationAttempts = businessServiceRequest.getNegotiationAttempts() == null
                        ? 0
                        : businessServiceRequest.getNegotiationAttempts();
                negotiationAttempts = negotiationAttempts + 1;
                businessServiceRequest.setNegotiationAttempts(negotiationAttempts);
            } else {
                businessServiceRequest.setNegotiationAttempts(request.getNegotiationAttemptsCount());
            }
        }

        if (request.getLastOfferedPrice() != null) {
            businessServiceRequest.setLastOfferedPrice(request.getLastOfferedPrice());
        }

        if (businessServiceRequest.getRequestStatus() == null) {
            businessServiceRequest.setRequestStatus(ServiceRequestStatus.NEGOTIATING);
        }

        businessServiceRequestRepository.save(businessServiceRequest);

        return businessServiceRequest.dto();
    }

    @Override
    public PaginatedResponse<BusinessServiceRequestDto> listForBusiness(
            String businessId, String businessServiceId, Pageable pageable) {
        Page<BusinessServiceRequest> requestsPage;
        if (businessId != null && businessServiceId != null) {
            requestsPage = businessServiceRequestRepository.findByBusinessIdAndBusinessServiceId(
                    businessId, businessServiceId, pageable);
        } else if (businessId != null) {
            requestsPage = businessServiceRequestRepository.findByBusinessId(businessId, pageable);
        } else if (businessServiceId != null) {
            requestsPage = businessServiceRequestRepository.findByBusinessServiceId(businessServiceId, pageable);
        } else {
            requestsPage = businessServiceRequestRepository.findAll(pageable);
        }

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
    public PaginatedResponse<BusinessServiceRequestDto> listForBusinessService(String businessServiceId, Pageable pageable) {
        Page<BusinessServiceRequest> requestsPage = businessServiceRequestRepository.findByBusinessServiceId(businessServiceId, pageable);

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
    public PaginatedResponse<BusinessServiceRequestDto> listForUser(String id, Pageable pageable) {
        Page<BusinessServiceRequest> requestsPage = businessServiceRequestRepository.findByUserId(id, pageable);

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
