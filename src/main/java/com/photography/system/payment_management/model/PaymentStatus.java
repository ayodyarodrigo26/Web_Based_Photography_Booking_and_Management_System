package com.photography.system.payment_management.model;

/**
 * ADVANCE_PAID: partial payment (balance may remain).
 * COMPLETED: full amount received for this order.
 */
public enum PaymentStatus {
    ADVANCE_PAID,
    COMPLETED
}