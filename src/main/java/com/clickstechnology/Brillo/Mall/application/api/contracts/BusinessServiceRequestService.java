package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceRequestDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.PlaceBusinessServiceRequestPayload;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.ServiceRequestStatus;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface BusinessServiceRequestService {
    void create(PlaceBusinessServiceRequestPayload request, CustomerDto customer, BusinessServiceDto businessService);

    BusinessServiceRequestDto findById(String requestId);

    BusinessServiceRequestDto updateRequest(String requestId, PlaceBusinessServiceRequestPayload request, boolean businessActor);

    PaginatedResponse<BusinessServiceRequestDto> listForBusiness(
            String businessId,
            String businessServiceId,
            ServiceRequestStatus requestStatus,
            Pageable pageable);

    PaginatedResponse<BusinessServiceRequestDto> listForBusinesses(
            Set<String> businessIds,
            ServiceRequestStatus requestStatus,
            Pageable pageable);

    PaginatedResponse<BusinessServiceRequestDto> listForBusinessService(
            String businessServiceId,
            ServiceRequestStatus requestStatus,
            Pageable pageable);

    PaginatedResponse<BusinessServiceRequestDto> listForUser(
            String id,
            ServiceRequestStatus requestStatus,
            Pageable pageable);

    void delete(BusinessServiceRequestDto request);

    void markBooked(String requestId);

    void ensureBelongsToUser(BusinessServiceRequestDto serviceRequest, String userId);

    void ensureBelongsToService(BusinessServiceRequestDto serviceRequest, String businessServiceId);
}
