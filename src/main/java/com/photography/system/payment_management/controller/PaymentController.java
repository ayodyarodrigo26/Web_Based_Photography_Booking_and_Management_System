package com.photography.system.payment_management.controller;

import com.photography.system.booking_management.entity.Booking;
import com.photography.system.booking_management.service.BookingService;
import com.photography.system.payment_management.dto.PaymentRequest;
import com.photography.system.payment_management.model.Payment;
import com.photography.system.payment_management.model.PaymentStatus;
import com.photography.system.payment_management.service.PaymentService;
import com.photography.system.payment_management.service.ReceiptService;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private static final String SESSION_CHECKOUT_DRAFT = "CHECKOUT_DRAFT";

    private final PaymentService paymentService;
    private final ReceiptService receiptService;
    private final BookingService bookingService;

    public PaymentController(PaymentService paymentService,
                             ReceiptService receiptService,
                             BookingService bookingService) {
        this.paymentService = paymentService;
        this.receiptService = receiptService;
        this.bookingService = bookingService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BigDecimal.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isBlank()) {
                    setValue(BigDecimal.ZERO);
                } else {
                    setValue(new BigDecimal(text.trim()));
                }
            }
        });
    }

    @ExceptionHandler(BindException.class)
    public String handleBind(BindException ex, RedirectAttributes ra, HttpSession session) {
        Object target = ex.getBindingResult().getTarget();

        if (target instanceof PaymentRequest req) {
            session.setAttribute(SESSION_CHECKOUT_DRAFT, req);

            if (req.getBookingId() != null && !req.getBookingId().isBlank()) {
                ra.addFlashAttribute("error", "Invalid number in the form. Please check amounts and try again.");
                return "redirect:/payments?bookingId=" + req.getBookingId().trim();
            }
        }

        ra.addFlashAttribute("error", "Invalid number in the form. Please check amounts and try again.");
        return "redirect:/payments";
    }

    @GetMapping({"", "/"})
    public String paymentHub(Model model,
                             Authentication authentication,
                             HttpSession session,
                             @RequestParam(required = false) String myEmail,
                             @RequestParam(required = false) Long detailId,
                             @RequestParam(required = false) Boolean payBalance,
                             @RequestParam(required = false) Long bookingId) {

        boolean isAdmin = authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("payments", isAdmin ? paymentService.getAllPayments() : Collections.emptyList());
        model.addAttribute("payBalanceOpen", Boolean.TRUE.equals(payBalance));
        model.addAttribute("bookingPaymentMode", false);
        model.addAttribute("showPaymentNav", false);
        model.addAttribute("showPayBalanceSection", true);
        model.addAttribute("bookingForPayment", null);
        model.addAttribute("bookingTotal", BigDecimal.ZERO);
        model.addAttribute("prefillBalanceBookingId", "");
        model.addAttribute("prefillBalanceEmail", "");

        Object draftObj = session.getAttribute(SESSION_CHECKOUT_DRAFT);

        if (bookingId != null) {
            Booking booking = bookingService.getBooking(bookingId);
            Optional<Payment> existingPaymentOpt = paymentService.findLatestByBookingId(String.valueOf(bookingId));

            boolean hasExistingPayment = existingPaymentOpt.isPresent();
            boolean showBookingPayment = false;
            boolean showBalanceSection = true;

            if (hasExistingPayment) {
                Payment existingPayment = existingPaymentOpt.get();

                if (existingPayment.getPaymentStatus() == PaymentStatus.ADVANCE_PAID) {
                    model.addAttribute("prefillBalanceBookingId", booking.getId().toString());
                    model.addAttribute("prefillBalanceEmail", booking.getCustomerEmail());
                    showBalanceSection = true;
                } else {
                    showBalanceSection = false;
                }
            } else {
                showBookingPayment = true;
                showBalanceSection = false;
            }

            model.addAttribute("bookingPaymentMode", showBookingPayment);
            model.addAttribute("showPaymentNav", showBookingPayment);
            model.addAttribute("showPayBalanceSection", showBalanceSection);
            model.addAttribute("bookingForPayment", booking);
            model.addAttribute("bookingTotal",
                    booking.getFinalAmount() != null ? booking.getFinalAmount() : BigDecimal.ZERO);

            if (showBookingPayment) {
                PaymentRequest checkoutDraft;
                if (draftObj instanceof PaymentRequest draft
                        && draft.getBookingId() != null
                        && draft.getBookingId().equals(String.valueOf(bookingId))) {
                    checkoutDraft = draft;
                    session.removeAttribute(SESSION_CHECKOUT_DRAFT);
                } else {
                    checkoutDraft = buildDraftFromBooking(booking);
                }
                model.addAttribute("checkoutDraft", checkoutDraft);
            }
        } else if (draftObj instanceof PaymentRequest draft) {
            model.addAttribute("checkoutDraft", draft);
            session.removeAttribute(SESSION_CHECKOUT_DRAFT);
        }

        if (!isAdmin && myEmail != null && !myEmail.isBlank()) {
            model.addAttribute("myPayments", paymentService.findByCustomerEmail(myEmail.trim()));
            model.addAttribute("myEmail", myEmail.trim());
        } else {
            model.addAttribute("myPayments", Collections.emptyList());
        }

        if (detailId != null && isAdmin) {
            Payment payment = paymentService.getPaymentById(detailId);
            if (payment != null) {
                BigDecimal paid = payment.getAmountPaid() != null ? payment.getAmountPaid() : BigDecimal.ZERO;
                BigDecimal total = payment.getTotalAmount() != null ? payment.getTotalAmount() : BigDecimal.ZERO;
                BigDecimal balance = total.subtract(paid);
                if (balance.compareTo(BigDecimal.ZERO) < 0) {
                    balance = BigDecimal.ZERO;
                }
                model.addAttribute("detailPayment", payment);
                model.addAttribute("detailBalance", balance);
            }
        }

        return "payment_management/payment-hub";
    }

    private PaymentRequest buildDraftFromBooking(Booking booking) {
        PaymentRequest req = new PaymentRequest();
        req.setBookingId(String.valueOf(booking.getId()));
        req.setFullName(booking.getCustomerName());
        req.setEmail(booking.getCustomerEmail());
        req.setPhoneNumber(booking.getCustomerPhone());
        req.setEventDate(booking.getEventDate() != null ? booking.getEventDate().toString() : "");
        req.setPaymentPlan("FULL");
        return req;
    }

    @PostMapping("/process")
    public String process(@ModelAttribute PaymentRequest request,
                          RedirectAttributes ra,
                          Authentication authentication,
                          HttpSession session) {
        try {
            session.removeAttribute(SESSION_CHECKOUT_DRAFT);
            Payment saved = paymentService.processCheckout(request);

            boolean admin = authentication != null
                    && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            ra.addFlashAttribute("successAlert",
                    admin ? ("Payment #" + saved.getId() + " recorded successfully.")
                            : "Your payment was recorded successfully.");
            ra.addFlashAttribute("receiptPaymentId", saved.getId());

            if (request.getBookingId() != null && !request.getBookingId().isBlank()) {
                return "redirect:/payments?bookingId=" + request.getBookingId().trim();
            }

            return "redirect:/payments";
        } catch (IllegalArgumentException ex) {
            session.setAttribute(SESSION_CHECKOUT_DRAFT, request);
            ra.addFlashAttribute("error", ex.getMessage());

            if (request.getBookingId() != null && !request.getBookingId().isBlank()) {
                return "redirect:/payments?bookingId=" + request.getBookingId().trim();
            }

            return "redirect:/payments";
        } catch (DataAccessException ex) {
            session.setAttribute(SESSION_CHECKOUT_DRAFT, request);
            ra.addFlashAttribute("error", "Could not save to the database. Check that MySQL is running.");

            if (request.getBookingId() != null && !request.getBookingId().isBlank()) {
                return "redirect:/payments?bookingId=" + request.getBookingId().trim();
            }

            return "redirect:/payments";
        }
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes ra) {
        try {
            paymentService.deletePayment(id);
            ra.addFlashAttribute("successAlert", "Payment deleted.");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Could not delete payment.");
        }
        return "redirect:/payments";
    }

    @GetMapping("/view/{id}")
    public String viewPaymentDetails(@PathVariable Long id) {
        return "redirect:/payments?detailId=" + id;
    }

    @PostMapping("/pay-balance")
    public String payBalance(@RequestParam Long paymentId,
                             @RequestParam String bookingId,
                             @RequestParam String email,
                             @RequestParam BigDecimal balanceAmount,
                             @RequestParam("balancePaymentMethod") String balancePaymentMethod,
                             @RequestParam(required = false) String customerNote,
                             RedirectAttributes ra) {
        try {
            Payment payment = paymentService.recordCustomerBalancePayment(
                    paymentId, bookingId, email, balanceAmount, balancePaymentMethod, customerNote);

            ra.addFlashAttribute("successAlert",
                    "Your remaining balance was paid in full. Download your receipt below.");
            ra.addFlashAttribute("receiptPaymentId", payment.getId());

            if (bookingId != null && !bookingId.isBlank()) {
                return "redirect:/payments?bookingId=" + bookingId.trim();
            }

            return "redirect:/payments";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());

            if (bookingId != null && !bookingId.isBlank()) {
                return "redirect:/payments?bookingId=" + bookingId.trim() + "&payBalance=1";
            }

            return "redirect:/payments?payBalance=1";
        }
    }

    @GetMapping("/receipt/{id}")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long id) throws Exception {
        Payment payment = paymentService.getPaymentById(id);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found");
        }

        byte[] pdf = receiptService.generateReceipt(payment);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=receipt-" + id + ".pdf")
                .header("Content-Type", "application/pdf")
                .header("Cache-Control", "no-store, no-cache, must-revalidate")
                .body(pdf);
    }

    @GetMapping("/export")
    public void exportPaymentsToCSV(HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=payments.csv");

        List<Payment> payments = paymentService.getAllPayments();

        PrintWriter writer = response.getWriter();

        // Header
        writer.println("Payment ID,Customer Name,Email,Phone,Booking ID,Event Date,Amount Paid,Total Amount,Remaining Balance,Status");

        for (Payment p : payments) {

            writer.println(
                    p.getId() + "," +
                            p.getFullName() + "," +
                            p.getEmail() + "," +
                            p.getPhoneNumber() + "," +
                            p.getBookingId() + "," +
                            p.getEventDate() + "," +
                            p.getAmountPaid() + "," +
                            p.getTotalAmount() + "," +
                            p.getRemainingBalance() + "," +
                            p.getPaymentStatus()
            );
        }

        writer.flush();
        writer.close();
    }
}