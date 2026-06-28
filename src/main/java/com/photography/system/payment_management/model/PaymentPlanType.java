package com.photography.system.payment_management.model;

/** FULL = pay order total in one go; ADVANCE = partial payment (balance may remain). */
public enum PaymentPlanType {
    FULL,
    ADVANCE
}