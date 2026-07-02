package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface CustomerService {
    CustomerDto resolveCustomer(CustomerDto customer, String userId, Set<String> businessIds);

    PaginatedResponse<CustomerDto> findAllByRefs(Set<String> customerRefs, int page, int pageSize);

    CustomerDto findByPhoneOrEmail(String ownerId);

    CustomerDto findByUserId(String userId);

    Set<CustomerDto> findAllByRefs(Set<String> customerIds);

    void updateCustomer(String customerId, CustomerDto customer);

    PaginatedResponse<CustomerDto> findAllByBusinessId(String businessId, Pageable pageable);

    CustomerDto resolveWhatsappCustomer(String phoneNumber, String displayName, String businessId);
}
