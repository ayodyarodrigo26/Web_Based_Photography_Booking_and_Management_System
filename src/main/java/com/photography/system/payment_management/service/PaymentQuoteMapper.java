package com.photography.system.payment_management.service;

import com.photography.system.payment_management.dto.PaymentQuote;
import com.photography.system.payment_management.model.Payment;

import java.math.BigDecimal;

/** Copies calculated quote lines onto a Payment entity before persistence. */
public final class PaymentQuoteMapper {

    private PaymentQuoteMapper() {
    }

    public static void apply(Payment payment, PaymentQuote quote) {
        payment.setRawBaseAmount(quote.getRawBaseAmount());
        payment.setSeasonalAdjustmentAmount(quote.getSeasonalAdjustmentAmount());
        payment.setBaseAmount(quote.getAdjustedBaseAmount());
        payment.setExtraServicesAmount(quote.getExtraServicesAmount());
        payment.setExtraServicesDescription(quote.getExtraServicesDescription());
        payment.setPromotionalDiscountAmount(quote.getPromotionalDiscountAmount());
        payment.setManualDiscountAmount(BigDecimal.ZERO);
        payment.setDiscountAmount(quote.getTotalDiscountAmount());
        payment.setTotalAmount(quote.getTotalAmount());

        BigDecimal paid = payment.getAmountPaid() != null ? payment.getAmountPaid() : BigDecimal.ZERO;
        if (paid.compareTo(quote.getTotalAmount()) > 0) {
            paid = quote.getTotalAmount();
            payment.setAmountPaid(paid);
        }

        BigDecimal actualPaid = payment.getAmountPaid() != null ? payment.getAmountPaid() : BigDecimal.ZERO;
        payment.setRemainingBalance(quote.getTotalAmount().subtract(actualPaid).max(BigDecimal.ZERO));
    }
}