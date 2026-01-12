package com.catalyst.payment.application.dto;

import com.catalyst.payment.domain.model.Payment;
import com.catalyst.payment.domain.model.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for payment information.
 */
@Builder
public record PaymentDto(
    UUID id,
    UUID invoiceId,
    String stripePaymentIntentId,
    PaymentStatus status,
    BigDecimal amount,
    String currency,
    String paymentMethodType,
    String failureReason,
    boolean successful,
    LocalDateTime createdAt
) {
    /**
     * Creates a DTO from a Payment domain entity.
     *
     * @param payment the payment entity
     * @return the DTO
     */
    public static PaymentDto fromEntity(Payment payment) {
        return PaymentDto.builder()
            .id(payment.getId())
            .invoiceId(payment.getInvoiceId())
            .stripePaymentIntentId(payment.getStripePaymentIntentId() != null 
                ? payment.getStripePaymentIntentId().getValue() : null)
            .status(payment.getStatus())
            .amount(payment.getAmount().getAmount())
            .currency(payment.getAmount().getCurrencyCode())
            .paymentMethodType(payment.getPaymentMethodType())
            .failureReason(payment.getFailureReason())
            .successful(payment.isSuccessful())
            .createdAt(payment.getCreatedAt())
            .build();
    }
}

