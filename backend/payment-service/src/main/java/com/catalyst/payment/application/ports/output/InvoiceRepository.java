package com.catalyst.payment.application.ports.output;

import com.catalyst.payment.domain.model.Invoice;
import com.catalyst.payment.domain.model.InvoiceStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for invoice persistence operations.
 */
public interface InvoiceRepository {
    
    /**
     * Saves an invoice.
     *
     * @param invoice the invoice to save
     * @return the saved invoice
     */
    Invoice save(Invoice invoice);

    /**
     * Finds an invoice by ID.
     *
     * @param id the invoice ID
     * @return an optional containing the invoice if found
     */
    Optional<Invoice> findById(UUID id);

    /**
     * Finds an invoice by Stripe invoice ID.
     *
     * @param stripeInvoiceId the Stripe invoice ID
     * @return an optional containing the invoice if found
     */
    Optional<Invoice> findByStripeInvoiceId(String stripeInvoiceId);

    /**
     * Finds all invoices for a subscription.
     *
     * @param subscriptionId the subscription ID
     * @return list of invoices
     */
    List<Invoice> findBySubscriptionId(UUID subscriptionId);

    /**
     * Finds invoices by status.
     *
     * @param status the invoice status
     * @return list of invoices with the given status
     */
    List<Invoice> findByStatus(InvoiceStatus status);

    /**
     * Finds overdue invoices.
     *
     * @return list of overdue invoices
     */
    List<Invoice> findOverdueInvoices();

    /**
     * Deletes an invoice.
     *
     * @param id the invoice ID
     */
    void deleteById(UUID id);
}

