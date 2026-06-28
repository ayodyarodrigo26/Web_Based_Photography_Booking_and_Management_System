package com.photography.system.booking_management.controller;

import com.photography.system.booking_management.entity.Booking;
import com.photography.system.booking_management.entity.BookingPaymentStatus;
import com.photography.system.booking_management.entity.BookingStatus;
import com.photography.system.booking_management.service.BookingService;
import com.photography.system.payment_management.model.Payment;
import com.photography.system.payment_management.model.PaymentStatus;
import com.photography.system.payment_management.service.PaymentService;
import com.photography.system.user_management.model.User;
import com.photography.system.user_management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/booking")
public class BookingController {

    private static final String RESERVATION_SUMMARY_SESSION_KEY = "RESERVATION_SUMMARY";


    private final BookingService bookingService;
    private final UserService userService;
    private final PaymentService paymentService;

    public BookingController(BookingService bookingService,
                             UserService userService,
                             PaymentService paymentService) {

        this.bookingService = bookingService;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    @GetMapping("/new")
    public String showForm(Authentication authentication, HttpSession session, Model model) {
        Booking booking = new Booking();

        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userService.getByEmail(email);

            if (user != null) {
                booking.setCustomerName(user.getFullName());
                booking.setCustomerEmail(user.getEmail());
                booking.setCustomerPhone(user.getPhone());
            }
        }

        Object reservationSummary = session.getAttribute(RESERVATION_SUMMARY_SESSION_KEY);
        if (reservationSummary != null) {
            model.addAttribute("reservationSummary", reservationSummary);
        }

        model.addAttribute("booking", booking);
        model.addAttribute("photographers", bookingService.getAllPhotographers());
        model.addAttribute("selectedPhotographerId", null);
        return "booking_management/booking-form";
    }

    @PostMapping("/create")
    public String createOrUpdateBooking(@ModelAttribute("booking") Booking booking,
                                        @RequestParam Long photographerId,
                                        HttpSession session,
                                        Model model) {
        try {
            Object reservationSummaryObj = session.getAttribute(RESERVATION_SUMMARY_SESSION_KEY);

            if (reservationSummaryObj instanceof Map<?, ?> reservationSummary) {
                Object packageNamesObj = reservationSummary.get("packageNames");
                Object finalTotal = reservationSummary.get("finalTotal");

                if (packageNamesObj instanceof List<?> packageNames && !packageNames.isEmpty()) {
                    // join multiple package names into one string
                    String joinedNames = String.join(", ",
                            packageNames.stream()
                                    .map(Object::toString)
                                    .toList()
                    );

                    booking.setPackageName(joinedNames);
                }

                if (finalTotal != null) {
                    try {
                        booking.setFinalAmount(new BigDecimal(finalTotal.toString()));
                    } catch (Exception ignored) {
                    }
                }
            }

            booking.setPaymentStatus(BookingPaymentStatus.UNPAID);
            booking.setHoldUntil(LocalDateTime.now().plusMinutes(10));

            Booking saved = bookingService.createBooking(booking, photographerId);
            return "redirect:/booking/summary/" + saved.getId();

        } catch (RuntimeException ex) {
            Object reservationSummary = session.getAttribute(RESERVATION_SUMMARY_SESSION_KEY);
            if (reservationSummary != null) {
                model.addAttribute("reservationSummary", reservationSummary);
            }

            model.addAttribute("error", ex.getMessage());
            model.addAttribute("booking", booking);
            model.addAttribute("photographers", bookingService.getAllPhotographers());
            model.addAttribute("selectedPhotographerId", photographerId);
            return "booking_management/booking-form";
        }
    }

    @GetMapping("/summary/{id}")
    public String showSummary(@PathVariable Long id,
                              Model model,
                              HttpSession session,   // ✅ ADD THIS
                              RedirectAttributes ra) {

        try {
            Booking booking = bookingService.getBooking(id);
            model.addAttribute("booking", booking);

            // 🔥 ADD THIS BLOCK
            Object summaryObj = session.getAttribute("RESERVATION_SUMMARY");

            if (summaryObj instanceof Map<?, ?> summary) {
                model.addAttribute("loyaltyDiscount", summary.get("loyaltyDiscount"));
                model.addAttribute("promotionDiscount", summary.get("promotionDiscount"));
                model.addAttribute("couponDiscount", summary.get("couponDiscount"));
            }

            return "booking_management/booking-summary";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Booking not found");
            return "redirect:/booking/admin";
        }
    }

    @GetMapping("/track")
    public String showTrackForm() {
        return "booking_management/booking-track";
    }

    @GetMapping("/track/{id}")
    public String trackBooking(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            Booking booking = bookingService.getBooking(id);
            model.addAttribute("booking", booking);

            Payment latestPayment = paymentService.findLatestByBookingId(String.valueOf(id)).orElse(null);
            model.addAttribute("latestPayment", latestPayment);

            BigDecimal amountPaid = BigDecimal.ZERO;
            BigDecimal remainingBalance = BigDecimal.ZERO;
            boolean fullyPaid = false;
            boolean advancePaid = false;

            if (latestPayment != null) {
                amountPaid = latestPayment.getAmountPaid() != null ? latestPayment.getAmountPaid() : BigDecimal.ZERO;
                remainingBalance = latestPayment.getRemainingBalance() != null ? latestPayment.getRemainingBalance() : BigDecimal.ZERO;
                fullyPaid = latestPayment.getPaymentStatus() == PaymentStatus.COMPLETED;
                advancePaid = latestPayment.getPaymentStatus() == PaymentStatus.ADVANCE_PAID;
            }

            model.addAttribute("amountPaid", amountPaid);
            model.addAttribute("remainingBalance", remainingBalance);
            model.addAttribute("fullyPaid", fullyPaid);
            model.addAttribute("advancePaid", advancePaid);

            return "booking_management/booking-track-result";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", "Verification Failed: ID not found.");
            return "redirect:/booking/track";
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id,
                                @RequestParam("reason") String reason,
                                RedirectAttributes ra) {
        try {
            bookingService.cancelBooking(id, reason);
            ra.addFlashAttribute("message", "Your reservation has been formally cancelled.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", "Cancellation failed: " + ex.getMessage());
        }
        return "redirect:/booking/track/" + id;
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("bookings", bookingService.getAllBookings());
        return "booking_management/booking-list-admin";
    }

    @PostMapping("/admin/delete/{id}")
    public String adminDeleteBooking(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookingService.deleteBookingById(id);
            ra.addFlashAttribute("message", "Record #" + id + " was permanently removed from archives.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to delete record: " + e.getMessage());
        }
        return "redirect:/booking/admin";
    }

    @PostMapping("/admin/approve/{id}")
    public String approveBooking(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookingService.updateStatus(id, BookingStatus.APPROVED);
            ra.addFlashAttribute("message", "Booking #" + id + " Approved.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Approval failed.");
        }
        return "redirect:/booking/admin";
    }

    @PostMapping("/admin/reject/{id}")
    public String rejectBooking(@PathVariable Long id,
                                @RequestParam("reason") String reason,
                                RedirectAttributes ra) {
        try {
            bookingService.rejectBooking(id, reason);
            ra.addFlashAttribute("message", "Booking #" + id + " Rejected. Reason sent to customer.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not complete rejection: " + e.getMessage());
        }
        return "redirect:/booking/admin";
    }

    @GetMapping("/admin/calendar")
    public String showAdminCalendar(Model model) {
        List<Booking> bookings = bookingService.getAllBookings();
        List<Map<String, Object>> eventList = new ArrayList<>();

        if (bookings != null) {
            for (Booking b : bookings) {
                Map<String, Object> event = new HashMap<>();
                event.put("id", b.getId());
                event.put("title", b.getCustomerName() + " | " + b.getEventLocation());
                event.put("start", b.getEventDate().toString() + "T" + b.getStartTime().toString());
                event.put("end", b.getEventDate().toString() + "T" + b.getEndTime().toString());
                event.put("color", b.getStatus() == BookingStatus.APPROVED ? "#2e7d32" : "#6e4a36");
                eventList.add(event);
            }
        }

        model.addAttribute("bookings", eventList);
        return "booking_management/booking-calender-admin";
    }

    @GetMapping("/user/bookings")
    public String userBookings(Authentication auth, Model model) {

        // ✅ FIX 1: prevent Whitelabel error
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        String email = auth.getName();

        // ✅ FIX 2: safe call
        List<Booking> bookings = bookingService.getBookingsForUser(email);

        model.addAttribute("bookings", bookings);

        return "user_management/user/bookings";
    }

    @PostMapping("/delete/{id}")
    public String deleteBooking(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes ra) {

        try {
            bookingService.deleteBookingById(id);

            // optional: clear session summary (safe cleanup)
            session.removeAttribute("RESERVATION_SUMMARY");

            ra.addFlashAttribute("message", "Booking deleted successfully.");

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Delete failed: " + e.getMessage());
        }

        return "redirect:/user/cart";
    }

    @GetMapping("/bookings")
    public String photographerBookings(Authentication auth, Model model) {

        User photographer = userService.getByEmail(auth.getName());

        List<Booking> bookings =
                bookingService.getBookingsForPhotographer(photographer.getId());

        model.addAttribute("bookings", bookings);

        return "user_management/photographer/bookings";
    }






}