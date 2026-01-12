package com.catalyst.payment.application.dto;

import com.catalyst.payment.domain.model.Invoice;
import com.catalyst.payment.domain.model.InvoiceStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for invoice information.
 */
@Builder
public record InvoiceDto(
    UUID id,
    UUID subscriptionId,
    String stripeInvoiceId,
    InvoiceStatus status,
    BigDecimal amountDue,
    BigDecimal amountPaid,
    String currency,
    String invoicePdfUrl,
    String hostedInvoiceUrl,
    LocalDateTime dueDate,
    LocalDateTime paidAt,
    boolean paid,
    boolean overdue,
    LocalDateTime createdAt
) {
    /**
     * Creates a DTO from an Invoice domain entity.
     *
     * @param invoice the invoice entity
     * @return the DTO
     */
    public static InvoiceDto fromEntity(Invoice invoice) {
        return InvoiceDto.builder()
            .id(invoice.getId())
            .subscriptionId(invoice.getSubscriptionId())
            .stripeInvoiceId(invoice.getStripeInvoiceId() != null 
                ? invoice.getStripeInvoiceId().getValue() : null)
            .status(invoice.getStatus())
            .amountDue(invoice.getAmountDue().getAmount())
            .amountPaid(invoice.getAmountPaid().getAmount())
            .currency(invoice.getAmountDue().getCurrencyCode())
            .invoicePdfUrl(invoice.getInvoicePdfUrl())
            .hostedInvoiceUrl(invoice.getHostedInvoiceUrl())
            .dueDate(invoice.getDueDate())
            .paidAt(invoice.getPaidAt())
            .paid(invoice.isPaid())
            .overdue(invoice.isOverdue())
            .createdAt(invoice.getCreatedAt())
            .build();
    }
}

