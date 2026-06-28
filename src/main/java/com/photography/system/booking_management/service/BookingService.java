package com.photography.system.booking_management.service;

import com.photography.system.booking_management.entity.Booking;
import com.photography.system.booking_management.entity.BookingStatus;
import com.photography.system.booking_management.repository.BookingRepository;
import com.photography.system.user_management.model.User;
import com.photography.system.user_management.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

import java.time.LocalDate;
import com.photography.system.booking_management.entity.BookingPaymentStatus;
import com.photography.system.feedback_management.FeedbackRepo;


@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final FeedbackRepo feedbackRepo;

    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository,
                          FeedbackRepo feedbackRepo) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.feedbackRepo = feedbackRepo;
    }

    public List<User> getAllPhotographers() {
        return userRepository.findByRole_Name("ROLE_PHOTOGRAPHER");
    }

    public Booking createBooking(Booking booking, Long photographerId) {
        User photographer = userRepository.findById(photographerId)
                .orElseThrow(() -> new RuntimeException("Photographer not found"));

        boolean isPhotographer = photographer.getRole() != null
                && "ROLE_PHOTOGRAPHER".equals(photographer.getRole().getName());

        if (!isPhotographer) {
            throw new RuntimeException("Selected user is not a photographer");
        }

        boolean conflict;
        if (booking.getId() != null) {
            conflict = bookingRepository.existsConflictExcludingId(
                    photographerId,
                    booking.getEventDate(),
                    List.of(BookingStatus.PENDING, BookingStatus.APPROVED),
                    booking.getEndTime(),
                    booking.getStartTime(),
                    booking.getId()
            );
        } else {
            conflict = bookingRepository
                    .existsByPhotographer_IdAndEventDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                            photographerId,
                            booking.getEventDate(),
                            List.of(BookingStatus.PENDING, BookingStatus.APPROVED),
                            booking.getEndTime(),
                            booking.getStartTime()
                    );
        }

        if (conflict) {
            throw new RuntimeException("Photographer is not available for selected time");
        }

        booking.setPhotographer(photographer);

        if (booking.getId() == null) {
            booking.setStatus(BookingStatus.PENDING);
        } else {
            Booking existing = getBooking(booking.getId());
            booking.setStatus(existing.getStatus());
            booking.setCancellationMessage(existing.getCancellationMessage());
        }

        return bookingRepository.save(booking);
    }

    public Booking cancelBooking(Long id, String reason) {
        Booking booking = getBooking(id);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationMessage(reason);

        return bookingRepository.save(booking);
    }

    public Booking rejectBooking(Long id, String reason) {
        Booking booking = getBooking(id);

        booking.setStatus(BookingStatus.REJECTED);
        booking.setCancellationMessage(reason);

        return bookingRepository.save(booking);
    }

    public Booking getBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
    }

    public void deleteBookingById(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new RuntimeException("Booking not found with id: " + id);
        }
        bookingRepository.deleteById(id);
    }

    public Booking updateStatus(Long id, BookingStatus newStatus) {
        Booking booking = getBooking(id);
        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // ✅ FIXED (SAFE)
    public List<Booking> getBookingsForUser(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }
        return bookingRepository.findByCustomerEmailOrderByEventDateDesc(email.trim());
    }

    public List<Booking> getBookingsForPhotographer(Long photographerId) {
        if (photographerId == null) {
            return List.of();
        }
        return bookingRepository.findByPhotographer_IdOrderByEventDateDesc(photographerId);
    }

    public boolean canUserGiveFeedback(String email, Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId).orElse(null);

        if (booking == null) return false;

        // Must belong to user
        if (!booking.getCustomerEmail().equals(email)) return false;

        // Must be fully paid
        if (booking.getPaymentStatus() != BookingPaymentStatus.FULLY_PAID) return false;

        // Must be after event end
        if (booking.getEventDate() == null || booking.getEndTime() == null) return false;

        LocalDateTime eventEnd =
                LocalDateTime.of(booking.getEventDate(), booking.getEndTime());

        return LocalDateTime.now().isAfter(eventEnd);
    }

    public List<Booking> getEligibleBookingsForFeedback(String email) {

        List<Booking> bookings = bookingRepository.findByCustomerEmailOrderByEventDateDesc(email);

        return bookings.stream()
                .filter(b -> b.getPaymentStatus() == BookingPaymentStatus.FULLY_PAID)
                .filter(b -> b.getEventDate() != null && b.getEndTime() != null)
                .filter(b -> {
                    LocalDateTime eventEnd =
                            LocalDateTime.of(b.getEventDate(), b.getEndTime());
                    return LocalDateTime.now().isAfter(eventEnd);
                })
                .toList();
    }

    public boolean hasUserAlreadyGivenFeedback(Long bookingId, String customerEmail) {
        return feedbackRepo.existsByBooking_IdAndCustomerEmail(bookingId, customerEmail);
    }


}