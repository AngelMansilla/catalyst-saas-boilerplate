package com.catalyst.payment.domain.exception;

import com.catalyst.shared.domain.exception.DomainException;

/**
 * Base exception for payment-related domain errors.
 */
public class PaymentException extends DomainException {
    
    private static final String ERROR_CODE_PREFIX = "PAYMENT";
    
    public PaymentException(String message) {
        super(ERROR_CODE_PREFIX + ".ERROR", message);
    }

    public PaymentException(String errorCode, String message) {
        super(ERROR_CODE_PREFIX + "." + errorCode, message);
    }

    public PaymentException(String message, Throwable cause) {
        super(ERROR_CODE_PREFIX + ".ERROR", message, cause);
    }

    public PaymentException(String errorCode, String message, Throwable cause) {
        super(ERROR_CODE_PREFIX + "." + errorCode, message, cause);
    }

    @Override
    public int getHttpStatus() {
        return 400; // Bad Request by default
    }
}

