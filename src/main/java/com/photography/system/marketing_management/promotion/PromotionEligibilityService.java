package com.photography.system.marketing_management.promotion;

import com.photography.system.booking_management.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PromotionEligibilityService {

    private final BookingRepository bookingRepository;

    public PromotionEligibilityService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    //  Loyalty check
    public boolean isLoyalCustomer(String email) {
        if (email == null || email.isBlank()) return false;

        LocalDate oneYearAgo = LocalDate.now().minusMonths(12);

        long count = bookingRepository.countRecentBookings(
                email,
                LocalDate.now().minusMonths(12)
        );

        return count >= 3;
    }

    // (keep this to avoid breaking PaymentService)
    public Optional<Promotion> resolveEligiblePromotion(String code,
                                                        Object pkg,
                                                        LocalDate eventDate) {
        return Optional.empty();
    }
}