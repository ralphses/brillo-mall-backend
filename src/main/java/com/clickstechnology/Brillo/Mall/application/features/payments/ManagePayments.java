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
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBookingRequest;
import com.clickstechnology.Brillo.Mall.application.enums.BookingStatus;
import com.clickstechnology.Brillo.Mall.application.enums.OrderStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PayableType;
import com.clickstechnology.Brillo.Mall.application.enums.PaymentStatus;
import com.clickstechnology.Brillo.Mall.application.enums.UserRole;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.features.notifications.NotificationEventPublisher;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagePayments {

    private final PaymentService paymentService;
    private final TenantContextResolver tenantContextResolver;
    private final BookedBusinessServiceService bookedBusinessServiceService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final PaymentProcessorResolver paymentProcessorResolver;
    private final NotificationEventPublisher notificationEventPublisher;

    @Value("${payment.processor.default}")
    private String defaultPaymentProcessor;

    @LoggableRequest
    @Transactional
    public PaymentResponse initializePayment(PaymentInitializationRequest initializationRequest, HttpServletRequest httpServletRequest) {
        UserDto user = tenantContextResolver.currentUser(httpServletRequest);
        PaymentContext paymentContext = resolvePaymentContext(initializationRequest, httpServletRequest, user);

        Optional<PaymentLogDto> existingPaymentLog = paymentService.findByPayableTypeAndPayableId(
                initializationRequest.getPayableType(),
                initializationRequest.getPayableId()
        );

        PaymentLogDto paymentLog;
        String reference;
        if (existingPaymentLog.isPresent()) {
            paymentLog = existingPaymentLog.get();
            reference = paymentLog.getPaymentReference();
            if (hasGatewayCredentials(paymentLog) && paymentLog.getPaymentStatus() != PaymentStatus.FAILED) {
                return buildPaymentResponse(paymentLog);
            }
        } else {
            reference = AppUtils.generateUniqueReference();
            paymentService.createNewLog(
                    paymentContext.businessId,
                    user.getId(),
                    reference,
                    paymentContext.amount,
                    initializationRequest.getPayableType(),
                    initializationRequest.getPayableId(),
                    user.getEmail()
            );
            paymentLog = paymentService.findByPaymentReference(reference);
        }

        PaymentProcessor processor = paymentProcessorResolver.resolve(defaultPaymentProcessor);
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .amount(paymentContext.amount)
                .email(user.getEmail())
                .currency("NGN")
                .reference(reference)
                .build();

        PaymentResponse processorResponse = processor.initializePayment(paymentRequest);
        log.info(":::Payment response: {}", processorResponse);
        paymentService.updateInitialization(reference, processorResponse.getAuthorizationUrl(), processorResponse.getAccessCode(), PaymentStatus.PROCESSING);

        return PaymentResponse.builder()
                .authorizationUrl(processorResponse.getAuthorizationUrl())
                .accessCode(processorResponse.getAccessCode())
                .reference(reference)
                .paymentStatus(PaymentStatus.PROCESSING)
                .build();
    }

    @Transactional
    public VerificationResponse verifyPayment(String reference) {
        PaymentLogDto paymentLog = paymentService.findByPaymentReference(reference);

        if (paymentLog.getPaymentStatus() == PaymentStatus.PAID) {
            return VerificationResponse.builder()
                    .verified(true)
                    .message("Payment already verified")
                    .reference(reference)
                    .paymentStatus(PaymentStatus.PAID)
                    .reconciled(true)
                    .build();
        }

        PaymentProcessor processor = paymentProcessorResolver.resolve(defaultPaymentProcessor);
        paymentService.updateStatus(paymentLog.getId(), PaymentStatus.PROCESSING);
        VerificationResponse verification = processor.verifyPayment(reference);

        PaymentStatus targetStatus = verification.isVerified() ? PaymentStatus.PAID : PaymentStatus.FAILED;
        reconcilePayment(reference, targetStatus, "verification", verification.getMessage());

        return VerificationResponse.builder()
                .verified(verification.isVerified())
                .message(verification.getMessage())
                .reference(reference)
                .paymentStatus(targetStatus)
                .reconciled(targetStatus == PaymentStatus.PAID)
                .build();
    }

    public void handleWebHook(String processor, String signature, String payload, HttpServletRequest httpServletRequest) {
        PaymentProcessor paymentProcessor = paymentProcessorResolver.resolve(processor);
        paymentProcessor.handleWebHook(signature, payload);
    }

    @Transactional
    public void handlePaymentNotification(String paymentReference, PaymentStatus paymentStatus, String gatewayMessage) {
        reconcilePayment(paymentReference, paymentStatus, "webhook", gatewayMessage);
    }

    private void reconcilePayment(String paymentReference, PaymentStatus targetStatus, String source, String gatewayMessage) {
        PaymentLogDto paymentLog = paymentService.findByPaymentReference(paymentReference);

        if (paymentLog.getPaymentStatus() == targetStatus) {
            return;
        }

        if (paymentLog.getPaymentStatus() == PaymentStatus.PAID && targetStatus == PaymentStatus.FAILED) {
            log.info("Ignoring late failure for already paid reference {}", paymentReference);
            return;
        }

        if (paymentLog.getPaymentStatus() == PaymentStatus.REVERSED && targetStatus != PaymentStatus.REVERSED) {
            log.info("Ignoring late payment event for reversed reference {}", paymentReference);
            return;
        }

        if (!paymentLog.getPaymentStatus().canTransitionTo(targetStatus)) {
            throw new BusinessException("Invalid payment status transition.");
        }

        OrderDto order = null;
        BookedServiceDto booking = null;
        if (paymentLog.getPayableType() == PayableType.ORDER) {
            order = orderService.findOrderById(paymentLog.getPayableId());
        } else if (paymentLog.getPayableType() == PayableType.BOOKING) {
            booking = bookedBusinessServiceService.findById(paymentLog.getPayableId());
        }

        if (targetStatus == PaymentStatus.PAID) {
            if (paymentLog.getPayableType() == PayableType.ORDER) {
                orderService.updateOrderStatus(paymentLog.getPayableId(), OrderStatus.PAID);
            } else if (paymentLog.getPayableType() == PayableType.BOOKING) {
                bookedBusinessServiceService.updateBooking(
                        paymentLog.getPayableId(),
                        UpdateBookingRequest.builder().status(BookingStatus.CONFIRMED).build()
                );
            }
        }

        paymentService.updateStatusWithPaymentReference(paymentReference, targetStatus);
        if (targetStatus == PaymentStatus.PAID || targetStatus == PaymentStatus.FAILED || targetStatus == PaymentStatus.REVERSED) {
            String templateName = switch (targetStatus) {
                case PAID -> "payment_confirmed";
                case REVERSED -> "payment_reversed";
                default -> "payment_failed";
            };
            boolean success = targetStatus == PaymentStatus.PAID;
            publishPaymentNotification(paymentLog, order, booking, targetStatus, templateName, success);
        }
        log.info("Payment {} reconciled from {} using {}", paymentReference, source, gatewayMessage);
    }

    private void publishPaymentNotification(
            PaymentLogDto paymentLog,
            OrderDto order,
            BookedServiceDto booking,
            PaymentStatus targetStatus,
            String templateName,
            boolean success
    ) {
        if (notificationEventPublisher == null) {
            return;
        }
        notificationEventPublisher.publishPaymentUpdate(
                paymentLog,
                order != null ? order.getCustomer() : booking != null ? booking.getCustomer() : null,
                order != null ? order.getBusiness() : booking != null ? booking.getBusiness() : null,
                targetStatus,
                templateName,
                success
        );
    }

    private boolean hasGatewayCredentials(PaymentLogDto paymentLog) {
        return paymentLog.getAuthorizationUrl() != null && paymentLog.getAccessCode() != null;
    }

    private PaymentResponse buildPaymentResponse(PaymentLogDto paymentLog) {
        return PaymentResponse.builder()
                .authorizationUrl(paymentLog.getAuthorizationUrl())
                .accessCode(paymentLog.getAccessCode())
                .reference(paymentLog.getPaymentReference())
                .paymentStatus(paymentLog.getPaymentStatus())
                .build();
    }

    private PaymentContext resolvePaymentContext(
            final PaymentInitializationRequest initializationRequest,
            final HttpServletRequest httpServletRequest,
            final UserDto user) {
        String businessId;
        BigDecimal amount;
        List<String> userRoles = user.getRoles();

        if (initializationRequest.getPayableType() == PayableType.ORDER) {
            CustomerDto customer = resolveCustomerForUser(user);
            OrderDto order = orderService.findOrderDetailsForCustomer(initializationRequest.getPayableId(), customer.getId());
            businessId = order.getBusinessId();
            amount = order.getTotalAmount();

        } else if (initializationRequest.getPayableType() == PayableType.BOOKING) {
            BookedServiceDto booking = bookedBusinessServiceService.findById(initializationRequest.getPayableId());
            businessId = booking.getBusiness().getId();
            if (initializationRequest.isBusiness() && userRoles.contains(UserRole.ADMIN.name())) {
                tenantContextResolver.ensureBusinessOwnership(httpServletRequest, businessId);
            } else {
                bookedBusinessServiceService.ensureBookingBelongsToUser(booking, user.getId());
            }
            amount = booking.getAgreedPrice();
        } else {
            throw new BusinessException("Invalid payable type");
        }

        if (amount == null) {
            throw new BusinessException("A payment amount is required.");
        }

        return new PaymentContext(businessId, amount);
    }

    private CustomerDto resolveCustomerForUser(final UserDto user) {
        return customerService.findByUserId(user.getId());
    }

    private record PaymentContext(String businessId, BigDecimal amount) {
    }
}
