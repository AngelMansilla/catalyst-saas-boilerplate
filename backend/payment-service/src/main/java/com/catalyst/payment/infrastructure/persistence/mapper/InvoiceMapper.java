package com.catalyst.payment.infrastructure.persistence.mapper;

import com.catalyst.payment.domain.model.Invoice;
import com.catalyst.payment.domain.valueobject.Money;
import com.catalyst.payment.domain.valueobject.StripeInvoiceId;
import com.catalyst.payment.infrastructure.persistence.entity.InvoiceJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

/**
 * Mapper between Invoice domain entity and JPA entity.
 */
@Component
public class InvoiceMapper {

    /**
     * Maps JPA entity to domain entity.
     *
     * @param entity the JPA entity
     * @return the domain entity
     */
    public Invoice toDomain(InvoiceJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Currency currency = Currency.getInstance(entity.getCurrency());
        
        Invoice invoice = Invoice.create(
            entity.getSubscriptionId(),
            Money.of(entity.getAmountDue(), currency)
        );

        invoice.setId(entity.getId());
        invoice.setStatus(entity.getStatus());
        
        if (entity.getAmountPaid() != null) {
            invoice.setAmountPaid(Money.of(entity.getAmountPaid(), currency));
        }
        
        if (entity.getStripeInvoiceId() != null) {
            invoice.setStripeInvoiceId(StripeInvoiceId.of(entity.getStripeInvoiceId()));
        }
        
        invoice.setInvoicePdfUrl(entity.getInvoicePdfUrl());
        invoice.setHostedInvoiceUrl(entity.getHostedInvoiceUrl());
        invoice.setDueDate(entity.getDueDate());
        invoice.setPaidAt(entity.getPaidAt());
        invoice.setCreatedAt(entity.getCreatedAt());
        invoice.setUpdatedAt(entity.getUpdatedAt());

        return invoice;
    }

    /**
     * Maps domain entity to JPA entity.
     *
     * @param invoice the domain entity
     * @return the JPA entity
     */
    public InvoiceJpaEntity toJpaEntity(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.setId(invoice.getId());
        entity.setSubscriptionId(invoice.getSubscriptionId());
        entity.setStatus(invoice.getStatus());
        entity.setAmountDue(invoice.getAmountDue().getAmount());
        entity.setCurrency(invoice.getAmountDue().getCurrencyCode());
        
        if (invoice.getAmountPaid() != null) {
            entity.setAmountPaid(invoice.getAmountPaid().getAmount());
        }
        
        if (invoice.getStripeInvoiceId() != null) {
            entity.setStripeInvoiceId(invoice.getStripeInvoiceId().getValue());
        }
        
        entity.setInvoicePdfUrl(invoice.getInvoicePdfUrl());
        entity.setHostedInvoiceUrl(invoice.getHostedInvoiceUrl());
        entity.setDueDate(invoice.getDueDate());
        entity.setPaidAt(invoice.getPaidAt());
        entity.setCreatedAt(invoice.getCreatedAt());
        entity.setUpdatedAt(invoice.getUpdatedAt());

        return entity;
    }
}

