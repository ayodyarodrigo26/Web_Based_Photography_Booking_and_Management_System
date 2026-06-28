package com.photography.system.user_management.controller;

import com.photography.system.user_management.model.User;
import com.photography.system.user_management.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import com.photography.system.booking_management.entity.Booking;

import com.photography.system.booking_management.service.BookingService;


@Controller
@RequestMapping("/photographer")
public class PhotographerController {

    private final UserService userService;
    private final BookingService bookingService;

    public PhotographerController(UserService userService,
                                  BookingService bookingService) {
        this.userService = userService;
        this.bookingService = bookingService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User user = userService.getByEmail(auth.getName());
        model.addAttribute("user", user);
        return "user_management/photographer/dashboard";
    }

    @GetMapping("/bookings")
    public String photographerBookings(Authentication auth, Model model) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login?view=admin";
        }

        String email = auth.getName();

        User photographer = userService.getByEmail(email);

        // 🔥 IMPORTANT: get bookings for THIS photographer
        List<Booking> bookings = bookingService.getBookingsForPhotographer(photographer.getId());

        model.addAttribute("bookings", bookings);

        return "user_management/photographer/bookings";
    }
}