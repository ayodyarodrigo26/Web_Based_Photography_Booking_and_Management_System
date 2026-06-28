package com.photography.system.marketing_management.discounts;

import com.photography.system.user_management.model.User;
import com.photography.system.user_management.repository.UserRepository;
import com.photography.system.booking_management.repository.BookingRepository; // ✅ ADD
import com.photography.system.payment_management.repository.PaymentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/loyalty")
public class LoyaltyDiscountController {

    private final LoyaltyDiscountService service;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public LoyaltyDiscountController(LoyaltyDiscountService service,
                                     UserRepository userRepository,
                                     PaymentRepository paymentRepository,
                                     BookingRepository bookingRepository) {

        this.service = service;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public String page(Model model) {

        // ===== EXISTING DATA =====
        LoyaltyDiscount discount = service.getDiscount();
        double percentage = (discount != null) ? discount.getPercentage() : 0;
        long totalCustomers = userRepository.countByRole_Name("CUSTOMER");
        double totalSavings = paymentRepository.getTotalDiscountGiven();

        // ===== LOYAL CUSTOMER LOGIC =====
        long loyalCustomers = 0;

        List<User> users = userRepository.findAll();

        for (User user : users) {

            // Only check customers
            if (user.getRole() != null &&
                    "ROLE_CUSTOMER".equalsIgnoreCase(user.getRole().getName())) {

                long paidBookings =
                        bookingRepository.countPaidBookingsByEmail(user.getEmail());

                if (paidBookings >= 3) {
                    loyalCustomers++;
                }
            }
        }

        // ===== SEND TO VIEW =====
        model.addAttribute("discount", discount);
        model.addAttribute("percentage", percentage);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalSavings", totalSavings);
        model.addAttribute("loyalCustomers", loyalCustomers); // ⭐ IMPORTANT

        return "marketing_management/loyalty";
    }

    @PostMapping
    public String update(@RequestParam double percentage) {
        service.update(percentage);
        return "redirect:/admin/loyalty?updated";
    }
}