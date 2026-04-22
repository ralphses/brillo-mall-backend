package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceRequestDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.PlaceBusinessServiceRequestPayload;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

public interface BusinessServiceRequestService {
    void create(PlaceBusinessServiceRequestPayload request, CustomerDto customer, BusinessServiceDto businessService);

    BusinessServiceRequestDto findById(String requestId);

    BusinessServiceRequestDto updateRequest(String requestId, PlaceBusinessServiceRequestPayload request, boolean updateNegotiationCounter);

    PaginatedResponse<BusinessServiceRequestDto> listForBusiness(String businessId, String businessServiceId, Pageable pageable);

    PaginatedResponse<BusinessServiceRequestDto> listForBusinessService(String businessServiceId, Pageable pageable);

    PaginatedResponse<BusinessServiceRequestDto> listForUser(String id, Pageable pageable);

    void delete(BusinessServiceRequestDto request);

    void ensureBelongsToUser(BusinessServiceRequestDto serviceRequest, String userId);

    void ensureBelongsToService(BusinessServiceRequestDto serviceRequest, String businessServiceId);
}
