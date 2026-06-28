package com.photography.system.payment_management.dto;

import com.photography.system.payment_management.model.PhotographyPackageCode;

import java.math.BigDecimal;

/**
 * Server-calculated price breakdown for the UI and for persisting on the Payment entity.
 */
public class PaymentQuote {

    private PhotographyPackageCode packageCode;
    private BigDecimal rawBaseAmount;
    private BigDecimal seasonalAdjustmentAmount;
    private BigDecimal adjustedBaseAmount;
    private BigDecimal extraServicesAmount;
    private String extraServicesDescription;
    private BigDecimal subtotalBeforeDiscounts;
    private BigDecimal promotionalDiscountAmount;
    private BigDecimal totalDiscountAmount;
    private BigDecimal totalAmount;
    private String appliedPromoCode;
    private String promotionEligibilityMessage;

    public PaymentQuote() {
    }

    public PhotographyPackageCode getPackageCode() {
        return packageCode;
    }

    public void setPackageCode(PhotographyPackageCode packageCode) {
        this.packageCode = packageCode;
    }

    public BigDecimal getRawBaseAmount() {
        return rawBaseAmount;
    }

    public void setRawBaseAmount(BigDecimal rawBaseAmount) {
        this.rawBaseAmount = rawBaseAmount;
    }

    public BigDecimal getSeasonalAdjustmentAmount() {
        return seasonalAdjustmentAmount;
    }

    public void setSeasonalAdjustmentAmount(BigDecimal seasonalAdjustmentAmount) {
        this.seasonalAdjustmentAmount = seasonalAdjustmentAmount;
    }

    public BigDecimal getAdjustedBaseAmount() {
        return adjustedBaseAmount;
    }

    public void setAdjustedBaseAmount(BigDecimal adjustedBaseAmount) {
        this.adjustedBaseAmount = adjustedBaseAmount;
    }

    public BigDecimal getExtraServicesAmount() {
        return extraServicesAmount;
    }

    public void setExtraServicesAmount(BigDecimal extraServicesAmount) {
        this.extraServicesAmount = extraServicesAmount;
    }

    public String getExtraServicesDescription() {
        return extraServicesDescription;
    }

    public void setExtraServicesDescription(String extraServicesDescription) {
        this.extraServicesDescription = extraServicesDescription;
    }

    public BigDecimal getSubtotalBeforeDiscounts() {
        return subtotalBeforeDiscounts;
    }

    public void setSubtotalBeforeDiscounts(BigDecimal subtotalBeforeDiscounts) {
        this.subtotalBeforeDiscounts = subtotalBeforeDiscounts;
    }

    public BigDecimal getPromotionalDiscountAmount() {
        return promotionalDiscountAmount;
    }

    public void setPromotionalDiscountAmount(BigDecimal promotionalDiscountAmount) {
        this.promotionalDiscountAmount = promotionalDiscountAmount;
    }

    public BigDecimal getTotalDiscountAmount() {
        return totalDiscountAmount;
    }

    public void setTotalDiscountAmount(BigDecimal totalDiscountAmount) {
        this.totalDiscountAmount = totalDiscountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getAppliedPromoCode() {
        return appliedPromoCode;
    }

    public void setAppliedPromoCode(String appliedPromoCode) {
        this.appliedPromoCode = appliedPromoCode;
    }

    public String getPromotionEligibilityMessage() {
        return promotionEligibilityMessage;
    }

    public void setPromotionEligibilityMessage(String promotionEligibilityMessage) {
        this.promotionEligibilityMessage = promotionEligibilityMessage;
    }
}