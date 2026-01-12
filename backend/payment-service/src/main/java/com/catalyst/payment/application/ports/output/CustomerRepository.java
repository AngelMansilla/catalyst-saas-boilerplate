package com.catalyst.payment.application.ports.output;

import com.catalyst.payment.domain.model.Customer;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port for customer persistence operations.
 */
public interface CustomerRepository {
    
    /**
     * Saves a customer.
     *
     * @param customer the customer to save
     * @return the saved customer
     */
    Customer save(Customer customer);

    /**
     * Finds a customer by ID.
     *
     * @param id the customer ID
     * @return an optional containing the customer if found
     */
    Optional<Customer> findById(UUID id);

    /**
     * Finds a customer by user ID.
     *
     * @param userId the user ID
     * @return an optional containing the customer if found
     */
    Optional<Customer> findByUserId(UUID userId);

    /**
     * Finds a customer by Stripe customer ID.
     *
     * @param stripeCustomerId the Stripe customer ID
     * @return an optional containing the customer if found
     */
    Optional<Customer> findByStripeCustomerId(String stripeCustomerId);

    /**
     * Finds a customer by email.
     *
     * @param email the email address
     * @return an optional containing the customer if found
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Checks if a customer exists for the given user ID.
     *
     * @param userId the user ID
     * @return true if a customer exists
     */
    boolean existsByUserId(UUID userId);

    /**
     * Deletes a customer.
     *
     * @param id the customer ID
     */
    void deleteById(UUID id);
}

