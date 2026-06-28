package com.photography.system.payment_management.repository;

import com.photography.system.payment_management.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByOrderByIdDesc();

    List<Payment> findByFullNameContainingIgnoreCase(String fullName);

    List<Payment> findByEmailIgnoreCaseOrderByPaymentDateDesc(String email);

    Optional<Payment> findFirstByBookingIdIgnoreCaseOrderByIdDesc(String bookingId);

    Optional<Payment> findFirstByBookingIdIgnoreCaseAndEmailIgnoreCaseOrderByIdDesc(String bookingId, String email);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndBookingConfirmedTrue(String email);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Payment p")
    BigDecimal getTotalRevenue();

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p")
    BigDecimal getTotalCollected();

    @Query("SELECT COALESCE(SUM(p.discountAmount), 0) FROM Payment p")
    BigDecimal getTotalDiscounts();

    @Query("SELECT COALESCE(SUM(COALESCE(p.totalAmount, 0) - COALESCE(p.amountPaid, 0)), 0) FROM Payment p")
    BigDecimal getTotalOutstanding();

    @Query(value = "SELECT YEAR(payment_date), MONTH(payment_date), COALESCE(SUM(total_amount), 0) "
            + "FROM payments WHERE payment_date IS NOT NULL "
            + "GROUP BY YEAR(payment_date), MONTH(payment_date) "
            + "ORDER BY YEAR(payment_date), MONTH(payment_date)", nativeQuery = true)
    List<Object[]> getMonthlyRevenue();

    @Query(value = "SELECT YEAR(payment_date), WEEK(payment_date, 3), COALESCE(SUM(total_amount), 0) "
            + "FROM payments WHERE payment_date IS NOT NULL "
            + "GROUP BY YEAR(payment_date), WEEK(payment_date, 3) "
            + "ORDER BY YEAR(payment_date), WEEK(payment_date, 3)", nativeQuery = true)
    List<Object[]> getWeeklyRevenue();

    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :start AND :end")
    List<Payment> findByDateRange(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.discountAmount), 0) FROM Payment p")
    double getTotalDiscountGiven();
}