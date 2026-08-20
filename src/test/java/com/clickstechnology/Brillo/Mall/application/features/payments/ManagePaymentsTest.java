package com.clickstechnology.Brillo.Mall.application.features.payments;

import com.clickstechnology.Brillo.Mall.application.api.contracts.BookedBusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.CustomerService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.OrderService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.PaymentProcessor;
import com.clickstechnology.Brillo.Mall.application.api.contracts.PaymentProcessorResolver;
import com.clickstechnology.Brillo.Mall.application.api.contracts.PaymentService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.TenantContextResolver;
import com.clickstechnology.Brillo.Mall.application.dto.CustomerDto;
import com.clickstechnology.Brillo.Mall.application.dto.UserDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.BookedServiceDto;
import com.clickstechnology.Brillo.Mall.application.dto.order.OrderDto;
import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentInitializationRequest;
import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentLogDto;
import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentRequest;
import com.clickstechnology.Brillo.Mall.application.dto.payments.PaymentResponse;
import com.clickstechnology.Brillo.Mall.application.dto.payments.VerificationResponse;
import com.clickstechnology.Brillo.Mall.application.enums.BookingStatus;
import com.clickstechnology.Brillo.Mall.application.enums.OrderStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PayableType;
import com.clickstechnology.Brillo.Mall.application.enums.PaymentStatus;
import com.clickstechnology.Brillo.Mall.application.enums.UserRole;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagePaymentsTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private TenantContextResolver tenantContextResolver;

    @Mock
    private BookedBusinessServiceService bookedBusinessServiceService;

    @Mock
    private CustomerService customerService;

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentProcessorResolver paymentProcessorResolver;

    @Mock
    private PaymentProcessor paymentProcessor;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ManagePayments managePayments;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(managePayments, "defaultPaymentProcessor", "paystack");
    }

    @Test
    void initializePayment_shouldCreateLogAndReturnGatewayDetails() {
        UserDto user = UserDto.builder()
                .id("user-1")
                .username("user-1")
                .email("customer@example.com")
                .roles(List.of(UserRole.CUSTOMER.name()))
                .build();
        CustomerDto customer = CustomerDto.builder()
                .id("customer-1")
                .userId("user-1")
                .build();
        OrderDto order = OrderDto.builder()
                .id("order-1")
                .businessId("business-1")
                .totalAmount(BigDecimal.valueOf(12_500))
                .build();

        when(tenantContextResolver.currentUser(httpServletRequest)).thenReturn(user);
        when(customerService.findByUserId("user-1")).thenReturn(customer);
        when(orderService.findOrderDetailsForCustomer("order-1", "customer-1")).thenReturn(order);
        when(paymentService.findByPayableTypeAndPayableId(PayableType.ORDER, "order-1")).thenReturn(Optional.empty());
        when(paymentService.findByPaymentReference(anyString())).thenAnswer(invocation -> {
            String reference = invocation.getArgument(0);
            return PaymentLogDto.builder()
                    .id(reference)
                    .paymentReference(reference)
                    .paymentStatus(PaymentStatus.PENDING)
                    .build();
        });
        when(paymentProcessorResolver.resolve("paystack")).thenReturn(paymentProcessor);
        when(paymentProcessor.initializePayment(any())).thenAnswer(invocation -> {
            PaymentRequest request = invocation.getArgument(0);
            return PaymentResponse.builder()
                    .authorizationUrl("https://paystack.test/authorize")
                    .accessCode("access-code")
                    .reference(request.getReference())
                    .paymentStatus(PaymentStatus.PROCESSING)
                    .build();
        });

        PaymentResponse response = managePayments.initializePayment(
                new PaymentInitializationRequest(PayableType.ORDER, "order-1", false),
                httpServletRequest
        );

        ArgumentCaptor<PaymentRequest> requestCaptor = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(paymentProcessor).initializePayment(requestCaptor.capture());
        verify(paymentService).createNewLog(
                eq("business-1"),
                eq("user-1"),
                anyString(),
                eq(BigDecimal.valueOf(12_500)),
                eq(PayableType.ORDER),
                eq("order-1"),
                eq("customer@example.com")
        );
        verify(paymentService).updateInitialization(
                eq(requestCaptor.getValue().getReference()),
                eq("https://paystack.test/authorize"),
                eq("access-code"),
                eq(PaymentStatus.PROCESSING)
        );

        assertThat(response.getReference()).isEqualTo(requestCaptor.getValue().getReference());
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.PROCESSING);
    }

    @Test
    void initializePayment_shouldFail_whenCustomerRecordIsMissing() {
        UserDto user = UserDto.builder()
                .id("user-1")
                .username("user-1")
                .email("customer@example.com")
                .roles(List.of(UserRole.CUSTOMER.name()))
                .build();

        when(tenantContextResolver.currentUser(httpServletRequest)).thenReturn(user);
        when(customerService.findByUserId("user-1")).thenThrow(new BusinessException("Customer not found"));

        assertThatThrownBy(() -> managePayments.initializePayment(
                new PaymentInitializationRequest(PayableType.ORDER, "order-1", false),
                httpServletRequest
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Customer not found");
    }

    @Test
    void initializePayment_shouldFail_whenOrderDoesNotBelongToCustomerRecord() {
        UserDto user = UserDto.builder()
                .id("user-1")
                .username("user-1")
                .email("customer@example.com")
                .roles(List.of(UserRole.CUSTOMER.name()))
                .build();
        CustomerDto customer = CustomerDto.builder()
                .id("customer-1")
                .userId("user-1")
                .build();

        when(tenantContextResolver.currentUser(httpServletRequest)).thenReturn(user);
        when(customerService.findByUserId("user-1")).thenReturn(customer);
        when(orderService.findOrderDetailsForCustomer("order-1", "customer-1"))
                .thenThrow(new BusinessException("Order not found or does not belong to the customer"));

        assertThatThrownBy(() -> managePayments.initializePayment(
                new PaymentInitializationRequest(PayableType.ORDER, "order-1", false),
                httpServletRequest
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Order not found or does not belong to the customer");
    }

    @Test
    void verifyPayment_shouldReconcilePaidOrderOnce() {
        AtomicReference<PaymentLogDto> state = new AtomicReference<>(
                PaymentLogDto.builder()
                        .id("payment-ref")
                        .paymentReference("payment-ref")
                        .paymentStatus(PaymentStatus.PENDING)
                        .payableType(PayableType.ORDER)
                        .payableId("order-1")
                        .build()
        );

        when(paymentService.findByPaymentReference("payment-ref")).thenAnswer(invocation -> state.get());
        when(paymentProcessorResolver.resolve("paystack")).thenReturn(paymentProcessor);
        when(paymentProcessor.verifyPayment("payment-ref")).thenReturn(
                VerificationResponse.builder()
                        .verified(true)
                        .message("Verified")
                        .reference("payment-ref")
                        .paymentStatus(PaymentStatus.PAID)
                        .reconciled(true)
                        .build()
        );
        when(orderService.updateOrderStatus("order-1", OrderStatus.PAID)).thenReturn(OrderDto.builder().id("order-1").status(OrderStatus.PAID).build());

        verifyPaymentAndApplyState(state, "payment-ref");
        VerificationResponse secondResponse = managePayments.verifyPayment("payment-ref");

        assertThat(secondResponse.isVerified()).isTrue();
        assertThat(secondResponse.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        verify(orderService).updateOrderStatus("order-1", OrderStatus.PAID);
        verify(orderService, never()).updateOrderStatus("order-1", OrderStatus.CANCELLED);
    }

    @Test
    void handlePaymentNotification_shouldConfirmBookingOnlyOnce() {
        AtomicReference<PaymentLogDto> state = new AtomicReference<>(
                PaymentLogDto.builder()
                        .id("payment-ref")
                        .paymentReference("payment-ref")
                        .paymentStatus(PaymentStatus.PENDING)
                        .payableType(PayableType.BOOKING)
                        .payableId("booking-1")
                        .build()
        );

        when(paymentService.findByPaymentReference("payment-ref")).thenAnswer(invocation -> state.get());
        when(bookedBusinessServiceService.updateBooking(eq("booking-1"), any())).thenAnswer(invocation -> {
            state.get().setPaymentStatus(PaymentStatus.PAID);
            return BookedServiceDto.builder()
                    .id("booking-1")
                    .bookingStatus(BookingStatus.CONFIRMED)
                    .build();
        });

        managePayments.handlePaymentNotification("payment-ref", PaymentStatus.PAID, "charge.success");
        managePayments.handlePaymentNotification("payment-ref", PaymentStatus.PAID, "charge.success");

        verify(bookedBusinessServiceService).updateBooking(eq("booking-1"), any());
    }

    private void verifyPaymentAndApplyState(AtomicReference<PaymentLogDto> state, String reference) {
        doAnswer(invocation -> {
            state.get().setPaymentStatus(PaymentStatus.PROCESSING);
            return null;
        }).when(paymentService).updateStatus(eq("payment-ref"), eq(PaymentStatus.PROCESSING));
        doAnswer(invocation -> {
            state.get().setPaymentStatus(PaymentStatus.PAID);
            return null;
        }).when(paymentService).updateStatusWithPaymentReference(eq(reference), eq(PaymentStatus.PAID));

        VerificationResponse response = managePayments.verifyPayment(reference);
        assertThat(response.isVerified()).isTrue();
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }
}
