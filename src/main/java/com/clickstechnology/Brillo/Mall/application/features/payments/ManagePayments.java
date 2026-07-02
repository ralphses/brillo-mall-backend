package com.clickstechnology.Brillo.Mall.application.features.payments;

import com.clickstechnology.Brillo.Mall.application.api.contracts.AuthenticationUtil;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BookedBusinessServiceService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.BusinessService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.OrderService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.PaymentProcessor;
import com.clickstechnology.Brillo.Mall.application.api.contracts.PaymentProcessorResolver;
import com.clickstechnology.Brillo.Mall.application.api.contracts.PaymentService;
import com.clickstechnology.Brillo.Mall.application.api.contracts.UserService;
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
import com.clickstechnology.Brillo.Mall.application.enums.EntityStatus;
import com.clickstechnology.Brillo.Mall.application.enums.OrderStatus;
import com.clickstechnology.Brillo.Mall.application.enums.PayableType;
import com.clickstechnology.Brillo.Mall.application.enums.UserRole;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.utils.AppUtils;
import com.clickstechnology.Brillo.Mall.infrastructure.logging.LoggableRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagePayments {

    private final AuthenticationUtil authenticationUtil;
    private final PaymentService paymentService;
    private final UserService userService;
    private final BookedBusinessServiceService bookedBusinessServiceService;
    private final OrderService orderService;
    private final BusinessService businessService;
    private final PaymentProcessorResolver paymentProcessorResolver;

    @Value("${payment.processor.default}")
    private String defaultPaymentProcessor;

    @LoggableRequest
    public PaymentResponse initializePayment(PaymentInitializationRequest initializationRequest, HttpServletRequest httpServletRequest) {
        String username = authenticationUtil.getAuthenticatedUsername(httpServletRequest);
        UserDto user = userService.findByUsername(username);

        String reference = AppUtils.generateUniqueReference();
        BigDecimal amount;
        String email = user.getEmail();
        List<String> userRoles = user.getRoles();
        String businessId = "";

        if (initializationRequest.getPayableType() == PayableType.ORDER) {
            OrderDto order = orderService.findOrderDetailsForCustomer(initializationRequest.getPayableId(), user.getId());
            businessId = order.getBusinessId();
            if (initializationRequest.isBusiness() && userRoles.contains(UserRole.ADMIN.name())) {
                businessService.ensureBusinessBelongsToUser(businessId, user.getId());
            } else {
                orderService.ensureOrderBelongsToUser(order, user.getId());
            }
            amount = order.getTotalAmount();

        } else if (initializationRequest.getPayableType() == PayableType.BOOKING) {
            BookedServiceDto booking = bookedBusinessServiceService.findById(initializationRequest.getPayableId());
            businessId = booking.getBusiness().getId();
           if (initializationRequest.isBusiness() && userRoles.contains(UserRole.ADMIN.name())) {
               businessService.ensureBusinessBelongsToUser(businessId, user.getId());
           }
           else {
                bookedBusinessServiceService.ensureBookingBelongsToUser(booking, user.getId());
            }
            amount = booking.getAgreedPrice();
        } else {
            throw new BusinessException("Invalid payable type");
        }

        paymentService.createNewLog(
                businessId,
                user.getId(),
                reference,
                amount,
                initializationRequest.getPayableType(),
                initializationRequest.getPayableId(),
                email
        );

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .amount(amount)
                .email(email)
                .currency("NGN")
                .reference(reference)
                .build();

        PaymentProcessor processor = paymentProcessorResolver.resolve(defaultPaymentProcessor);

        return processor.initializePayment(paymentRequest);
    }

    public VerificationResponse verifyPayment(String reference) {
        PaymentLogDto paymentLog = paymentService.findByPaymentReference(reference);

        if (paymentLog.getStatus() == EntityStatus.ACTIVE) {
            return VerificationResponse.builder().verified(true).message("Payment already verified").build();
        }

        PaymentProcessor processor = paymentProcessorResolver.resolve(defaultPaymentProcessor);
        VerificationResponse verification = processor.verifyPayment(reference);

        if (verification.isVerified()) {
            paymentLog.setStatus(EntityStatus.ACTIVE);

            if (paymentLog.getPayableType() == PayableType.ORDER) {
                orderService.updateOrderStatus(paymentLog.getPayableId(), OrderStatus.PAID);
            } else if (paymentLog.getPayableType() == PayableType.BOOKING) {
                bookedBusinessServiceService.updateBooking(paymentLog.getPayableId(), UpdateBookingRequest.builder().status(BookingStatus.CONFIRMED).build());
            }
        } else {
            paymentLog.setStatus(EntityStatus.INACTIVE);
        }

        paymentService.updateStatus(paymentLog.getId(), paymentLog.getStatus());
        return verification;
    }

    public void handleWebHook(String processor, String payload, HttpServletRequest httpServletRequest) {
        PaymentProcessor paymentProcessor = paymentProcessorResolver.resolve(processor);
        paymentProcessor.handleWebHook(payload);
    }

    public void handlePaymentNotification(String paymentReference) {
        PaymentLogDto paymentLog = paymentService.findByPaymentReference(paymentReference);
        PayableType payableType = paymentLog.getPayableType();
        if (payableType == PayableType.BOOKING) {
            bookedBusinessServiceService.updateBooking(paymentLog.getPayableId(), UpdateBookingRequest.builder().status(BookingStatus.CONFIRMED).build());
        }

        if (payableType.equals(PayableType.ORDER)) {
            orderService.updateOrderStatus(paymentLog.getPayableId(), OrderStatus.PAID);
        }
        paymentService.updateStatus(paymentLog.getId(), EntityStatus.ACTIVE);
    }
}
