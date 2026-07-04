package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.AddBusinessServiceRequest;

import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessServiceRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface BusinessServiceService {
    BusinessServiceDto addService(AddBusinessServiceRequest request, UserDto userDto);

    BusinessServiceDto updateService(String serviceId, UpdateBusinessServiceRequest request, UserDto userDto);

    List<BusinessServiceDto> searchServices(String search);

    PaginatedResponse<BusinessServiceDto> listServices(String businessId, Pageable pageable);

    PaginatedResponse<BusinessServiceDto> listServices(
            String businessId,
            Integer page,
            Integer pageSize,
            String search,
            String category,
            PricingType pricingType,
            Boolean negotiable,
            Boolean requiresSchedule,
            EntityStatus status);

    BusinessServiceDto getByIdOrSlug(String serviceIdOrSlug);

    void deleteBusinessService(String businessServiceId);

    BusinessServiceDto findById(String businessServiceId);

    void validateForRequests(BusinessServiceDto businessService);

    void validateForBooking(BusinessServiceDto businessService);

    void validatePriceAgreed(BusinessServiceDto businessService, BigDecimal totalPrice);
}
