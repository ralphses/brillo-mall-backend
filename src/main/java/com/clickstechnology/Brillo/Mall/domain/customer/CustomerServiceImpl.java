package com.clickstechnology.Brillo.Mall.domain.customer;

import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
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
    public CustomerDto resolveCustomer(CustomerDto customer, String userId, Set<String> businessIds) {

        Customer foundCustomer = customerRepository.findByPhone(customer.getCustomerPhoneNumber())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setAddress(customer.getAddress());
                    newCustomer.setUserId(userId);
                    newCustomer.setRelatedBusinessIds(businessIds);
                    newCustomer.setName(customer.getCustomerName());
                    newCustomer.setEmail(customer.getCustomerEmail());
                    newCustomer.setPhone(customer.getCustomerPhoneNumber());
                    return customerRepository.save(newCustomer);
                });
        Set<String> relatedBusinessIds = foundCustomer.getRelatedBusinessIds();
        relatedBusinessIds.addAll(businessIds);
        foundCustomer.setRelatedBusinessIds(relatedBusinessIds);
        customerRepository.save(foundCustomer);
        return foundCustomer.dto();
    }

    @Override
    public PaginatedResponse<CustomerDto> findAllByRefs(Set<String> customerRefs,  int page, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
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
        log.debug("Finding Customer by Phone or Email: {}", ownerId);
        return customerRepository.findByPhone(ownerId)
                .or(() -> customerRepository.findByEmail(ownerId))
                .map(Customer::dto)
                .orElseThrow(() -> new BusinessException("Customer not found"));
    }

    @Override
    public CustomerDto findByUserId(String userId) {
        return customerRepository.findByUserId(userId)
                .map(Customer::dto)
                .orElseThrow(() -> new BusinessException("Customer not found"));
    }

    @Override
    public Set<CustomerDto> findAllByRefs(Set<String> customerIds) {
        return customerRepository.findAllByReferenceIn(customerIds)
                .stream()
                .map(Customer::dto)
                .collect(Collectors.toSet());
    }

    @Async
    @Override
    @Transactional
    public void updateCustomer(String customerId, CustomerDto customerDto) {
        Customer customer = customerRepository.findByReference(customerId)
                .orElseThrow(() -> new BusinessException("Invalid Customer Id"));

        if (customerDto.getCustomerEmail() != null) {
            customer.setEmail(customerDto.getCustomerEmail());
        }

        if (customerDto.getCustomerPhoneNumber() != null) {
            customer.setPhone(customerDto.getCustomerPhoneNumber());
        }

        if (customerDto.getCustomerName() != null) {
            customer.setName(customerDto.getCustomerName());
        }

        if (customerDto.getAddress() != null) {
            customer.setAddress(customerDto.getAddress());
        }

        customerRepository.save(customer);
    }

    @Override
    public PaginatedResponse<CustomerDto> findAllByBusinessId(String businessId, Pageable pageable) {
        Page<Customer> customerPage = customerRepository.findAllByRelatedBusinessIdsContaining(businessId, pageable);
        List<CustomerDto> items = customerPage.getContent().stream().map(Customer::dto).collect(Collectors.toList());
        return PaginatedResponse.<CustomerDto>builder()
                .page(pageable.getPageNumber() + 1)
                .perPage(pageable.getPageSize())
                .total(customerPage.getTotalElements())
                .totalPages(customerPage.getTotalPages())
                .hasNext(customerPage.hasNext())
                .hasPrevious(customerPage.hasPrevious())
                .items(items)
                .build();
    }
}
