package com.clickstechnology.Brillo.Mall.application.features.business;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.TenantContextResolver;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetBusinessCustomers {
    private final CustomerService customerService;
    private final BusinessService businessService;
    private final TenantContextResolver tenantContextResolver;

    @LoggableRequest
    public PaginatedResponse<CustomerDto> execute(String businessId, Integer page, Integer pageSize, HttpServletRequest httpServletRequest) {
        tenantContextResolver.ensureBusinessOwnership(httpServletRequest, businessId);

        Set<String> customerRefs = businessService.findBusinessCustomers(businessId);
        return customerService.findAllByRefs(customerRefs, page, pageSize);
    }
}
