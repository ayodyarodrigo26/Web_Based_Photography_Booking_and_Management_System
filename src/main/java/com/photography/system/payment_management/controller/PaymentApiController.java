package com.photography.system.payment_management.controller;

import com.photography.system.payment_management.dto.PaymentQuote;
import com.photography.system.payment_management.dto.PaymentRequest;
import com.photography.system.payment_management.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST JSON endpoints used by payment-hub.html:
 * live quote, booking ID check, balance lookup.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentApiController {

    private final PaymentService paymentService;

    public PaymentApiController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Returns PaymentQuote for package + extras + promo + event date (no DB write). */
    @PostMapping("/quote")
    public ResponseEntity<?> quote(@RequestBody(required = false) PaymentRequest request) {
        try {
            if (request == null) {
                request = new PaymentRequest();
            }
            PaymentQuote quote = paymentService.previewQuote(request);
            return ResponseEntity.ok(quote);
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Quote failed"));
        }
    }

    /**
     * Read-only check for Record a payment:
     * is this reservation ID allowed for a new row?
     */
    @PostMapping("/validate-booking-id")
    public ResponseEntity<Map<String, Object>> validateBookingId(@RequestBody(required = false) Map<String, String> body) {
        String bookingId = body != null ? body.get("bookingId") : null;
        return ResponseEntity.ok(paymentService.validateBookingIdForNewPaymentApi(bookingId));
    }

    /**
     * Look up outstanding balance when booking ID + email match an ADVANCE_PAID payment.
     */
    @PostMapping("/lookup-balance")
    public ResponseEntity<?> lookupBalance(@RequestBody(required = false) Map<String, String> body) {
        try {
            Map<String, String> requestBody = body != null ? body : new HashMap<>();
            Map<String, Object> data = paymentService.lookupBalanceForCustomer(
                    requestBody.get("bookingId"),
                    requestBody.get("email")
            );
            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Lookup failed"));
        }
    }
}