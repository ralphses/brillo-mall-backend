package com.clickstechnology.Brillo.Mall.application.features.business;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PricingType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListPublicServices {

    private final BusinessServiceService businessServiceService;

    public PaginatedResponse<BusinessServiceDto> execute(
            String businessId,
            Integer page,
            Integer pageSize,
            String q,
            String category,
            PricingType pricingType,
            Boolean negotiable,
            Boolean requiresSchedule) {
        log.info(":::Loading public services for q: {}, category: {}, businessId: {}", q, category, businessId);
        return businessServiceService.listServices(
                businessId,
                page,
                pageSize,
                q,
                category,
                pricingType,
                negotiable,
                requiresSchedule,
                EntityStatus.ACTIVE);
    }
}
