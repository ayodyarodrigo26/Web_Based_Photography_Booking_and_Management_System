package com.photography.system.feedback_management;

import com.photography.system.payment_management.repository.PaymentRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.photography.system.booking_management.service.BookingService;
import com.photography.system.booking_management.entity.Booking;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackRepo feedbackRepo;
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    public FeedbackController(FeedbackRepo feedbackRepo,
                              PaymentRepository paymentRepository,
                              BookingService bookingService) {
        this.feedbackRepo = feedbackRepo;
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
    }

    @GetMapping("/check")
    public String checkFeedbackAccess(Authentication authentication, Model model) {

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/auth/login?role=customer&next=/feedback/check";
        }

        boolean isCustomer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

        if (!isCustomer) {
            model.addAttribute("message",
                    "Feedback submission is available only for customer accounts. Please log in as a customer to continue.");
            return "feedback_management/feedback-denied";
        }

        String customerEmail = authentication.getName();


        return "redirect:/feedback/add";
    }

    @GetMapping("/add")
    public String showForm(Authentication authentication, Model model) {

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/auth/login?role=customer&next=/feedback/check";
        }

        String email = authentication.getName();

        var bookings = bookingService.getEligibleBookingsForFeedback(email);

        if (bookings.isEmpty()) {
            model.addAttribute("message",
                    "You can only leave feedback after completed bookings.");
            return "feedback_management/feedback-denied";
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("feedback", new Feedback());

        return "feedback_management/feedback-form";
    }

    @PostMapping("/save")
    public String saveFeedback(@Valid @ModelAttribute("feedback") Feedback feedback,
                               BindingResult bindingResult,
                               @RequestParam Long bookingId,
                               Authentication authentication,
                               RedirectAttributes ra,
                               Model model) {

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/auth/login?role=customer&next=/feedback/check";
        }

        String customerEmail = authentication.getName();

        //  1. Prevent duplicate feedback
        if (feedbackRepo.existsByBooking_IdAndCustomerEmail(bookingId, customerEmail)) {
            ra.addFlashAttribute("error", "Feedback already submitted for this booking.");
            return "redirect:/feedback/list";
        }

        //  2. Check eligibility
        if (!bookingService.canUserGiveFeedback(customerEmail, bookingId)) {
            model.addAttribute("error",
                    "You can only give feedback after completed bookings.");

            var bookings = bookingService.getEligibleBookingsForFeedback(customerEmail);
            model.addAttribute("bookings", bookings);

            return "feedback_management/feedback-form"; // stay in form
        }

        //  3. Validation
        if (feedback.getRating() == 0) {
            bindingResult.rejectValue("rating", "rating.empty", "Please select a rating");
        }

        if (bindingResult.hasErrors()) {
            var bookings = bookingService.getEligibleBookingsForFeedback(customerEmail);
            model.addAttribute("bookings", bookings);
            return "feedback_management/feedback-form";
        }

        //  4. Save
        System.out.println("BOOKING ID: " + bookingId);
        System.out.println("USER: " + authentication.getName());

        Booking booking = bookingService.getBooking(bookingId);
        feedback.setBooking(booking);
        feedback.setCustomerEmail(customerEmail);
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setApproved(false);

        try {
            feedbackRepo.save(feedback);
            System.out.println("✅ FEEDBACK SAVED SUCCESSFULLY");
        } catch (Exception e) {
            System.out.println("❌ ERROR WHILE SAVING FEEDBACK:");
            e.printStackTrace();
        }

        return "redirect:/feedback/success";
    }

    @GetMapping("/list")
    public String listFeedback(Model model) {
        model.addAttribute("feedbackList", feedbackRepo.findByApprovedTrue());
        return "feedback_management/feedback-list";
    }

    @GetMapping("/admin")
    public String adminList(Model model) {
        model.addAttribute("feedbackList", feedbackRepo.findAll());
        return "feedback_management/admin-feedback";
    }

    @GetMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {
        Feedback f = feedbackRepo.findById(id).orElse(null);
        if (f != null) {
            f.setApproved(true);
            feedbackRepo.save(f);
        }
        return "redirect:/feedback/admin";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        feedbackRepo.deleteById(id);
        return "redirect:/feedback/admin";
    }

    @GetMapping("/success")
    public String success() {
        return "feedback_management/success";
    }



}