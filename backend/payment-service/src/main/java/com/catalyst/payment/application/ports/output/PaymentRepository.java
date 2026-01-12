package com.catalyst.payment.application.ports.output;

import com.catalyst.payment.domain.model.Payment;
import com.catalyst.payment.domain.model.PaymentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for payment persistence operations.
 */
public interface PaymentRepository {
    
    /**
     * Saves a payment.
     *
     * @param payment the payment to save
     * @return the saved payment
     */
    Payment save(Payment payment);

    /**
     * Finds a payment by ID.
     *
     * @param id the payment ID
     * @return an optional containing the payment if found
     */
    Optional<Payment> findById(UUID id);

    /**
     * Finds a payment by Stripe payment intent ID.
     *
     * @param stripePaymentIntentId the Stripe payment intent ID
     * @return an optional containing the payment if found
     */
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    /**
     * Finds all payments for an invoice.
     *
     * @param invoiceId the invoice ID
     * @return list of payments
     */
    List<Payment> findByInvoiceId(UUID invoiceId);

    /**
     * Finds payments by status.
     *
     * @param status the payment status
     * @return list of payments with the given status
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Deletes a payment.
     *
     * @param id the payment ID
     */
    void deleteById(UUID id);
}

