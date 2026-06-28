package com.photography.system.payment_management.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity: one database row per customer payment / reservation
 * (advance or full; balance updates same row).
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String phoneNumber;

    /** Reference to the reservation from Epic 2 */
    private String bookingId;

    @Enumerated(EnumType.STRING)
    private PhotographyPackageCode packageCode;

    private BigDecimal rawBaseAmount;
    private BigDecimal seasonalAdjustmentAmount;
    private BigDecimal baseAmount;
    private BigDecimal extraServicesAmount;

    @Column(length = 512)
    private String extraServicesDescription;

    private BigDecimal discountAmount;
    private BigDecimal promotionalDiscountAmount;
    private BigDecimal manualDiscountAmount;

    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal remainingBalance;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String paymentMethod;
    private String promoCode;
    private String eventDate;

    @Enumerated(EnumType.STRING)
    private PaymentPlanType paymentPlan;

    private LocalDateTime paymentDate;

    /** True after a successful payment record */
    private boolean bookingConfirmed;

    /** Customer notes when paying balance */
    @Column(length = 1024)
    private String customerBalanceNotes;

    public Payment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
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

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
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

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getPromotionalDiscountAmount() {
        return promotionalDiscountAmount;
    }

    public void setPromotionalDiscountAmount(BigDecimal promotionalDiscountAmount) {
        this.promotionalDiscountAmount = promotionalDiscountAmount;
    }

    public BigDecimal getManualDiscountAmount() {
        return manualDiscountAmount;
    }

    public void setManualDiscountAmount(BigDecimal manualDiscountAmount) {
        this.manualDiscountAmount = manualDiscountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public PaymentPlanType getPaymentPlan() {
        return paymentPlan;
    }

    public void setPaymentPlan(PaymentPlanType paymentPlan) {
        this.paymentPlan = paymentPlan;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public boolean isBookingConfirmed() {
        return bookingConfirmed;
    }

    public void setBookingConfirmed(boolean bookingConfirmed) {
        this.bookingConfirmed = bookingConfirmed;
    }

    public String getCustomerBalanceNotes() {
        return customerBalanceNotes;
    }

    public void setCustomerBalanceNotes(String customerBalanceNotes) {
        this.customerBalanceNotes = customerBalanceNotes;
    }


}