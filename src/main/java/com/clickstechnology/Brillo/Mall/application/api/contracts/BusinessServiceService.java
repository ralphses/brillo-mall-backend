package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.AddBusinessServiceRequest;

import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface BusinessServiceService {
    BusinessServiceDto addService(AddBusinessServiceRequest request, UserDto userDto);

    BusinessServiceDto updateService(String serviceId, UpdateBusinessServiceRequest request, UserDto userDto);

    PaginatedResponse<BusinessServiceDto> listServices(String businessId, Pageable pageable);

    BusinessServiceDto getByIdOrSlug(String serviceIdOrSlug);

    void deleteBusinessService(String businessServiceId);

    BusinessServiceDto findById(String businessServiceId);

    void validateForRequests(BusinessServiceDto businessService);

    void validatePriceAgreed(BusinessServiceDto businessService, BigDecimal totalPrice);
}