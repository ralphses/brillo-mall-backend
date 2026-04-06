package com.clickstechnology.Brillo.Mall.domain.customer;

import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public CustomerDto resolveCustomer(CustomerDto customer) {

        return customerRepository.findByPhone(customer.getCustomerPhoneNumber())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setAddress(customer.getAddress());
                    newCustomer.setName(customer.getCustomerName());
                    newCustomer.setEmail(customer.getCustomerEmail());
                    newCustomer.setPhone(customer.getCustomerPhoneNumber());
                    return customerRepository.save(newCustomer);
                }).dto();
    }

    @Override
    public PaginatedResponse<CustomerDto> findAllByRefs(Set<String> customerRefs,  int page, int pageSize) {
        Pageable pageable = PageRequest.of(Math.min(0, page-1), pageSize);
        Page<Customer> customerPage = customerRepository.findAllByReferenceIn(customerRefs, pageable);

        List<CustomerDto> items = customerPage.getContent().stream().map(Customer::dto).collect(Collectors.toList());

        return PaginatedResponse.<CustomerDto>builder()
                .page(page)
                .perPage(pageSize)
                .total((int) customerPage.getTotalElements())
                .totalPages(customerPage.getTotalPages())
                .hasNext(customerPage.hasNext())
                .hasPrevious(customerPage.hasPrevious())
                .items(items)
                .build();
    }

    @Override
    public CustomerDto findByPhoneOrEmail(String ownerId) {
        return customerRepository.findByPhone(ownerId)
                .or(() -> customerRepository.findByEmail(ownerId))
                .map(Customer::dto)
                .orElseThrow(() -> new BusinessException("Customer not found"));
    }
}