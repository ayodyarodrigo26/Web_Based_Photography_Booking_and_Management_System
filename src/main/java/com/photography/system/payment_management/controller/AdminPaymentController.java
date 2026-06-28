package com.photography.system.payment_management.controller;

import com.photography.system.payment_management.model.Payment;
import com.photography.system.payment_management.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import com.photography.system.booking_management.entity.Booking;
import com.photography.system.booking_management.service.BookingService;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/payments")
public class AdminPaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService; // ✅ ADD THIS

    public AdminPaymentController(PaymentService paymentService,
                                  BookingService bookingService) { // ✅ UPDATE CONSTRUCTOR
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public String adminPayments(@RequestParam(required = false) String keyword, Model model) {

        List<Payment> payments;

        if (keyword != null && !keyword.isBlank()) {
            payments = paymentService.searchPayments(keyword.trim());
            model.addAttribute("keyword", keyword.trim());
        } else {
            payments = paymentService.getAllPayments();
            model.addAttribute("keyword", "");
        }

        // ✅ FIX: Prepare eventDate properly
        List<Map<String, Object>> paymentView = payments.stream().map(p -> {

            Map<String, Object> map = new HashMap<>();
            map.put("payment", p);

            String eventDate = p.getEventDate();

            // 🔥 If empty → get from booking
            if ((eventDate == null || eventDate.isBlank()) && p.getBookingId() != null) {
                try {
                    Booking booking = bookingService.getBooking(Long.parseLong(p.getBookingId()));
                    if (booking != null && booking.getEventDate() != null) {
                        eventDate = booking.getEventDate().toString();
                    }
                } catch (Exception ignored) {}
            }

            map.put("eventDate", eventDate != null ? eventDate : "-");

            return map;
        }).collect(Collectors.toList());

        model.addAttribute("payments", paymentView); // ✅ IMPORTANT CHANGE

        model.addAttribute("totalRevenue", paymentService.getRevenue());
        model.addAttribute("totalCollected", paymentService.getCollected());
        model.addAttribute("outstandingReceivables", paymentService.getOutstandingReceivables());

        return "payment_management/admin-payment-list";
    }
}
