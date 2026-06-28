package com.photography.system.payment_management.service;

import com.photography.system.marketing_management.promotion.Promotion;
import com.photography.system.payment_management.dto.PaymentQuote;
import com.photography.system.payment_management.model.PhotographyPackageCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class PricingService {

    private static final BigDecimal WEEKEND_SURCHARGE_RATE = new BigDecimal("0.10");
    private static final BigDecimal DECEMBER_DISCOUNT_RATE = new BigDecimal("0.20");
    private static final BigDecimal SUMMER_DISCOUNT_RATE = new BigDecimal("0.05");

    public PaymentQuote buildQuote(
            PhotographyPackageCode pkg,
            BigDecimal extraServicesAmount,
            String extraServicesDescription,
            String eventDateStr,
            Promotion activePromotion,
            String requestedPromoCode,
            boolean isLoyalCustomer) {

        BigDecimal rawBase = pkg.getListPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal factor = BigDecimal.ONE;
        LocalDate eventDate = null;

        if (eventDateStr != null && !eventDateStr.isBlank()) {
            try {
                eventDate = LocalDate.parse(eventDateStr);
            } catch (Exception ignored) {}
        }

        if (eventDate != null) {
            int month = eventDate.getMonthValue();

            if (month == 12) {
                factor = factor.multiply(BigDecimal.ONE.subtract(DECEMBER_DISCOUNT_RATE));
            }

            if (month == 6 || month == 8) {
                factor = factor.multiply(BigDecimal.ONE.subtract(SUMMER_DISCOUNT_RATE));
            }

            int dow = eventDate.getDayOfWeek().getValue();
            if (dow >= 6) {
                factor = factor.multiply(BigDecimal.ONE.add(WEEKEND_SURCHARGE_RATE));
            }
        }

        BigDecimal adjustedBase = rawBase.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal seasonalAdjustment = adjustedBase.subtract(rawBase);

        BigDecimal extras = extraServicesAmount != null
                ? extraServicesAmount.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal subtotal = adjustedBase.add(extras);

        // 🔥 PROMO DISCOUNT
        BigDecimal promoDiscount = BigDecimal.ZERO;
        String promoMsg = "";
        String appliedCode = null;

        if (activePromotion != null) {
            BigDecimal percent = BigDecimal.valueOf(activePromotion.getDiscountPercent());
            promoDiscount = subtotal.multiply(percent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP);

            appliedCode = activePromotion.getTitle();
            promoMsg = "Promotion applied: " + appliedCode;
        }

        // 🔥 LOYALTY DISCOUNT
        BigDecimal loyaltyDiscount = BigDecimal.ZERO;

        if (isLoyalCustomer) {
            loyaltyDiscount = subtotal.multiply(new BigDecimal("0.05"))
                    .setScale(2, RoundingMode.HALF_UP);
            promoMsg += " + Loyalty discount applied";
        }

        BigDecimal totalDiscount = promoDiscount.add(loyaltyDiscount);

        if (totalDiscount.compareTo(subtotal) > 0) {
            totalDiscount = subtotal;
        }

        BigDecimal total = subtotal.subtract(totalDiscount).setScale(2, RoundingMode.HALF_UP);

        PaymentQuote quote = new PaymentQuote();
        quote.setPackageCode(pkg);
        quote.setRawBaseAmount(rawBase);
        quote.setSeasonalAdjustmentAmount(seasonalAdjustment);
        quote.setAdjustedBaseAmount(adjustedBase);
        quote.setExtraServicesAmount(extras);
        quote.setSubtotalBeforeDiscounts(subtotal);
        quote.setPromotionalDiscountAmount(promoDiscount);
        quote.setTotalDiscountAmount(totalDiscount);
        quote.setTotalAmount(total);
        quote.setAppliedPromoCode(appliedCode);
        quote.setPromotionEligibilityMessage(promoMsg);

        return quote;
    }
}