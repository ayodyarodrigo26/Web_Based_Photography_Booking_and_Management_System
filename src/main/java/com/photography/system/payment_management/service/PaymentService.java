package com.photography.system.payment_management.service;

import com.photography.system.booking_management.entity.Booking;
import com.photography.system.booking_management.entity.BookingPaymentStatus;
import com.photography.system.booking_management.repository.BookingRepository;
import com.photography.system.marketing_management.promotion.Promotion;
import com.photography.system.marketing_management.promotion.PromotionEligibilityService;
import com.photography.system.payment_management.dto.PaymentQuote;
import com.photography.system.payment_management.dto.PaymentRequest;
import com.photography.system.payment_management.model.*;
import com.photography.system.payment_management.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PricingService pricingService;
    private final PromotionEligibilityService promotionEligibilityService;
    private final BookingRepository bookingRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          PricingService pricingService,
                          PromotionEligibilityService promotionEligibilityService,
                          BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.pricingService = pricingService;
        this.promotionEligibilityService = promotionEligibilityService;
        this.bookingRepository = bookingRepository;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAllByOrderByIdDesc();
    }

    public List<Payment> findByCustomerEmail(String email) {
        if (email == null || email.isBlank()) return List.of();
        return paymentRepository.findByEmailIgnoreCaseOrderByPaymentDateDesc(email.trim());
    }

    public Optional<Payment> findLatestByBookingId(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) return Optional.empty();
        return paymentRepository.findFirstByBookingIdIgnoreCaseOrderByIdDesc(bookingId.trim());
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    // ==============================
    // ✅ PREVIEW WITH LOYALTY
    // ==============================
    public PaymentQuote previewQuote(PaymentRequest request) {

        validateEventDateNotInPast(request.getEventDate());

        PhotographyPackageCode pkg = PhotographyPackageCode.fromCode(request.getPackageCode());
        LocalDate eventDate = parseEventDate(request.getEventDate());

        Optional<Promotion> promo = promotionEligibilityService.resolveEligiblePromotion(
                request.getPromoCode(), pkg, eventDate
        );

        boolean isLoyal = promotionEligibilityService.isLoyalCustomer(request.getEmail());

        return pricingService.buildQuote(
                pkg,
                request.getExtras(),
                request.getExtraServicesDescription(),
                request.getEventDate(),
                promo.orElse(null),
                request.getPromoCode(),
                isLoyal   // ✅ NEW
        );
    }

    // ==============================
    // MAIN CHECKOUT
    // ==============================
    @Transactional
    public Payment processCheckout(PaymentRequest request) {

        String bookingId = normalizeBookingId(request.getBookingId());

        if (bookingId != null) {
            Optional<Booking> bookingOpt = findBookingByBookingId(bookingId);
            if (bookingOpt.isPresent()) {
                return processBookingCheckout(request, bookingOpt.get());
            }
        }

        return processStandaloneCheckout(request);
    }

    // ==============================
    // ✅ STANDALONE WITH LOYALTY
    // ==============================
    private Payment processStandaloneCheckout(PaymentRequest request) {

        validateEventDateNotInPast(request.getEventDate());

        PhotographyPackageCode pkg = PhotographyPackageCode.fromCode(request.getPackageCode());
        LocalDate eventDate = parseEventDate(request.getEventDate());

        Optional<Promotion> promo = promotionEligibilityService.resolveEligiblePromotion(
                request.getPromoCode(), pkg, eventDate
        );

        boolean isLoyal = promotionEligibilityService.isLoyalCustomer(request.getEmail());

        PaymentQuote quote = pricingService.buildQuote(
                pkg,
                request.getExtras(),
                request.getExtraServicesDescription(),
                request.getEventDate(),
                promo.orElse(null),
                request.getPromoCode(),
                isLoyal   // ✅ NEW
        );

        Payment payment = new Payment();

        payment.setFullName(request.getFullName());
        payment.setEmail(request.getEmail());
        payment.setPhoneNumber(request.getPhoneNumber());
        payment.setBookingId(normalizeBookingId(request.getBookingId()));
        payment.setPackageCode(pkg);
        payment.setPromoCode(quote.getAppliedPromoCode());
        payment.setEventDate(request.getEventDate());

        PaymentQuoteMapper.apply(payment, quote);

        BigDecimal toPay = request.getPaymentAmount().setScale(2, RoundingMode.HALF_UP);

        payment.setAmountPaid(toPay);
        payment.setRemainingBalance(quote.getTotalAmount().subtract(toPay));

        if (toPay.compareTo(quote.getTotalAmount()) < 0) {
            payment.setPaymentStatus(PaymentStatus.ADVANCE_PAID);
        } else {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
        }

        payment.setPaymentDate(LocalDateTime.now());
        payment.setBookingConfirmed(true);
        payment.setPaymentMethod(request.getPaymentMethod());

        return paymentRepository.save(payment);
    }

    // ==============================
    // BOOKING CHECKOUT
    // ==============================
    private Payment processBookingCheckout(PaymentRequest request, Booking booking) {

        BigDecimal total = booking.getFinalAmount();

        BigDecimal toPay = request.getPaymentAmount().setScale(2, RoundingMode.HALF_UP);

        Payment payment = new Payment();

        payment.setFullName(booking.getCustomerName());
        payment.setEmail(booking.getCustomerEmail());
        payment.setPhoneNumber(booking.getCustomerPhone());
        payment.setBookingId(String.valueOf(booking.getId()));
        payment.setTotalAmount(total);
        payment.setAmountPaid(toPay);
        payment.setRemainingBalance(total.subtract(toPay));

        if (toPay.compareTo(total) < 0) {
            payment.setPaymentStatus(PaymentStatus.ADVANCE_PAID);
        } else {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
        }

        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(request.getPaymentMethod());

        Payment saved = paymentRepository.save(payment);

        updateBookingAfterPayment(booking,
                saved.getPaymentStatus() == PaymentStatus.COMPLETED);

        return saved;
    }

    // ==============================
    // BALANCE PAYMENT
    // ==============================
    @Transactional
    public Payment recordCustomerBalancePayment(long paymentId,
                                                String bookingId,
                                                String email,
                                                BigDecimal amount,
                                                String paymentMethod,
                                                String customerNote) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found."));

        BigDecimal total = payment.getTotalAmount();
        BigDecimal paid = payment.getAmountPaid();

        BigDecimal newPaid = paid.add(amount);
        payment.setAmountPaid(newPaid);

        BigDecimal balance = total.subtract(newPaid);
        payment.setRemainingBalance(balance);
        payment.setPaymentMethod(paymentMethod);

        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
        }

        Payment saved = paymentRepository.save(payment);

        findBookingByBookingId(payment.getBookingId()).ifPresent(b ->
                updateBookingAfterPayment(b,
                        saved.getPaymentStatus() == PaymentStatus.COMPLETED));

        return saved;
    }

    // ==============================
    // BOOKING UPDATE FIX
    // ==============================
    private void updateBookingAfterPayment(Booking booking, boolean fullyPaid) {

        booking.setHoldUntil(null);

        if (fullyPaid) {
            booking.setPaymentStatus(BookingPaymentStatus.FULLY_PAID);
        } else {
            booking.setPaymentStatus(BookingPaymentStatus.ADVANCE_PAID);
        }

        bookingRepository.save(booking);
    }

    // ==============================
    // HELPERS
    // ==============================
    private Optional<Booking> findBookingByBookingId(String bookingId) {
        try {
            return bookingRepository.findById(Long.parseLong(bookingId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String normalizeBookingId(String raw) {
        return (raw == null || raw.isBlank()) ? null : raw.trim();
    }

    private static LocalDate parseEventDate(String raw) {
        try {
            return LocalDate.parse(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private static void validateEventDateNotInPast(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Event date cannot be in the past.");
        }
    }

    // ==============================
// ✅ REQUIRED FOR PaymentApiController
// ==============================

    public Map<String, Object> validateBookingIdForNewPaymentApi(String rawBookingId) {

        Map<String, Object> response = new HashMap<>();

        String bookingId = normalizeBookingId(rawBookingId);

        if (bookingId == null) {
            response.put("valid", false);
            response.put("message", "Booking ID is required.");
            return response;
        }

        Optional<Payment> existing = paymentRepository
                .findFirstByBookingIdIgnoreCaseOrderByIdDesc(bookingId);

        if (existing.isPresent()) {
            Payment p = existing.get();

            if (p.getPaymentStatus() == PaymentStatus.COMPLETED) {
                response.put("valid", false);
                response.put("message", "Already fully paid.");
                return response;
            }

            response.put("valid", false);
            response.put("message", "Already has pending balance.");
            return response;
        }

        response.put("valid", true);
        response.put("message", "");
        return response;
    }


// ==============================

    public Map<String, Object> lookupBalanceForCustomer(String bookingId, String email) {

        String bId = normalizeBookingId(bookingId);

        if (bId == null || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Booking ID and email required.");
        }

        Optional<Payment> opt = paymentRepository
                .findFirstByBookingIdIgnoreCaseAndEmailIgnoreCaseOrderByIdDesc(bId, email.trim());

        if (opt.isEmpty()) {
            throw new IllegalArgumentException("No payment found.");
        }

        Payment payment = opt.get();

        if (payment.getPaymentStatus() != PaymentStatus.ADVANCE_PAID) {
            throw new IllegalArgumentException("No balance to pay.");
        }

        BigDecimal total = payment.getTotalAmount();
        BigDecimal paid = payment.getAmountPaid();
        BigDecimal balance = total.subtract(paid);

        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", payment.getId());
        result.put("totalAmount", total);
        result.put("amountPaid", paid);
        result.put("balance", balance);

        return result;
    }


    // ==============================
// ✅ ADMIN FUNCTIONS (REQUIRED)
// ==============================

    public List<Payment> searchPayments(String name) {
        if (name == null || name.isBlank()) {
            return paymentRepository.findAllByOrderByIdDesc();
        }
        return paymentRepository.findByFullNameContainingIgnoreCase(name.trim());
    }


// ==============================

    public BigDecimal getRevenue() {
        return nz(paymentRepository.getTotalRevenue());
    }

    public BigDecimal getCollected() {
        return nz(paymentRepository.getTotalCollected());
    }

    public BigDecimal getTotalDiscounts() {
        return nz(paymentRepository.getTotalDiscounts());
    }

    public BigDecimal getOutstandingReceivables() {
        return nz(paymentRepository.getTotalOutstanding());
    }


// ==============================
// HELPER (if missing)
// ==============================

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}