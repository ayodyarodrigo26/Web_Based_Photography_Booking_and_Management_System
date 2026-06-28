package com.photography.system.booking_management.repository;

import com.photography.system.booking_management.entity.Booking;
import com.photography.system.booking_management.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByPhotographer_IdAndEventDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Long photographerId,
            LocalDate eventDate,
            List<BookingStatus> statuses,
            LocalTime endTime,
            LocalTime startTime
    );

    @Query("""
           select (count(b) > 0) from Booking b
           where b.photographer.id = :photographerId
             and b.eventDate = :eventDate
             and b.status in :statuses
             and b.startTime < :endTime
             and b.endTime > :startTime
             and b.id <> :excludeId
           """)
    boolean existsConflictExcludingId(
            @Param("photographerId") Long photographerId,
            @Param("eventDate") LocalDate eventDate,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("endTime") LocalTime endTime,
            @Param("startTime") LocalTime startTime,
            @Param("excludeId") Long excludeId
    );

    List<Booking> findByCustomerPhone(String customerPhone);

    List<Booking> findByEventLocationContainingIgnoreCase(String location);

    List<Booking> findByCustomerEmailOrderByEventDateDesc(String email);

    // ✅ FINAL CORRECT LOYALTY QUERY
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE LOWER(b.customerEmail) = LOWER(:email)
          AND b.eventDate <= CURRENT_DATE
          AND b.eventDate >= :sinceDate
    """)
    long countRecentBookings(@Param("email") String email,
                             @Param("sinceDate") LocalDate sinceDate);

    List<Booking> findByPhotographer_IdOrderByEventDateDesc(Long photographerId);


    @Query("""
    SELECT COUNT(b) FROM Booking b
    WHERE LOWER(b.customerEmail) = LOWER(:email)
      AND b.paymentStatus = com.photography.system.booking_management.entity.BookingPaymentStatus.FULLY_PAID
""")
    long countPaidBookingsByEmail(@Param("email") String email);






}