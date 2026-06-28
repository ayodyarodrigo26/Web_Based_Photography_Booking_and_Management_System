package com.photography.system.feedback_management;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepo extends JpaRepository<Feedback, Long> {
    List<Feedback> findByApprovedTrue();
    boolean existsByBooking_IdAndCustomerEmail(Long bookingId, String customerEmail);
}