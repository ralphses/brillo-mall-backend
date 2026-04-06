package com.clickstechnology.Brillo.Mall.application.api.contracts;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;

import java.util.Set;

public interface CustomerService {
    CustomerDto resolveCustomer(CustomerDto customer);

    PaginatedResponse<CustomerDto> findAllByRefs(Set<String> customerRefs, int page, int pageSize);

    CustomerDto findByPhoneOrEmail(String ownerId);
}
