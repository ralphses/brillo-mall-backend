package com.clickstechnology.Brillo.Mall.domain.customer;

import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void resolveCustomer_shouldCreateCustomerForAuthenticatedUser_whenMissing() {
        CustomerDto request = CustomerDto.builder()
                .customerName("Ada Lovelace")
                .customerEmail("ada@example.com")
                .customerPhoneNumber("07035002025")
                .address("1 Test Street")
                .build();

        Set<String> businessIds = new HashSet<>(Set.of("biz-1", "biz-2"));

        when(customerRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setReference("customer-ref-1");
            return saved;
        });

        CustomerDto result = customerService.resolveCustomer(request, "user-1", businessIds);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository, org.mockito.Mockito.times(2)).save(captor.capture());

        Customer saved = captor.getAllValues().getLast();
        assertThat(saved.getUserId()).isEqualTo("user-1");
        assertThat(saved.getPhone()).isEqualTo("07035002025");
        assertThat(saved.getRelatedBusinessIds()).containsExactlyInAnyOrder("biz-1", "biz-2");
        assertThat(result.getId()).isEqualTo("customer-ref-1");
        assertThat(result.getUserId()).isEqualTo("user-1");
    }

    @Test
    void resolveCustomer_shouldReuseCustomerForAuthenticatedUser_whenPresent() {
        Customer existing = Customer.builder()
                .userId("user-1")
                .name("Ada Lovelace")
                .phone("07035002025")
                .build();
        existing.setReference("customer-ref-1");
        existing.getRelatedBusinessIds().add("biz-1");

        CustomerDto request = CustomerDto.builder()
                .customerName("Ada Lovelace")
                .customerEmail("ada@example.com")
                .customerPhoneNumber("07035002025")
                .address("1 Test Street")
                .build();

        when(customerRepository.findByUserId("user-1")).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerDto result = customerService.resolveCustomer(request, "user-1", Set.of("biz-2"));

        assertThat(result.getId()).isEqualTo("customer-ref-1");
        assertThat(result.getUserId()).isEqualTo("user-1");
        assertThat(existing.getRelatedBusinessIds()).containsExactlyInAnyOrder("biz-1", "biz-2");
        verify(customerRepository).findByUserId(eq("user-1"));
        verify(customerRepository).save(existing);
    }
}
