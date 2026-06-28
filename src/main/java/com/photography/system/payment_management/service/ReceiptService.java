package com.photography.system.payment_management.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.photography.system.payment_management.model.Payment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/** Builds a simple PDF receipt for download from /payments/receipt/{id}. */
@Service
public class ReceiptService {

    /** @return PDF bytes (caller sets HTTP headers for download). */
    public byte[] generateReceipt(Payment payment) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("PAYMENT RECEIPT"));
        document.add(new Paragraph("--------------------------------"));
        document.add(new Paragraph("Payment record #: " + payment.getId()));

        if (payment.getBookingId() != null && !payment.getBookingId().isBlank()) {
            document.add(new Paragraph("Reservation / booking ref: " + payment.getBookingId()));
        }

        document.add(new Paragraph("Customer: " + safe(payment.getFullName())));
        document.add(new Paragraph("Email: " + safe(payment.getEmail())));

        if (payment.getPackageCode() != null) {
            document.add(new Paragraph("Package: " + payment.getPackageCode().getDisplayName()));
        }

        document.add(new Paragraph("Package subtotal: LKR " + safe(payment.getBaseAmount())));
        document.add(new Paragraph("Extras: LKR " + safe(payment.getExtraServicesAmount())));

        if (payment.getDiscountAmount() != null && payment.getDiscountAmount().signum() > 0) {
            document.add(new Paragraph("Discounts: LKR " + payment.getDiscountAmount()));
        }

        document.add(new Paragraph("Total: LKR " + safe(payment.getTotalAmount())));
        document.add(new Paragraph("Paid: LKR " + safe(payment.getAmountPaid())));
        document.add(new Paragraph("Balance: LKR " + safe(payment.getRemainingBalance())));
        document.add(new Paragraph("Status: " + (payment.getPaymentStatus() != null ? payment.getPaymentStatus() : "")));
        document.add(new Paragraph("Method: " + safe(payment.getPaymentMethod())));
        document.add(new Paragraph("Date: " + (payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : "")));

        if (payment.getCustomerBalanceNotes() != null && !payment.getCustomerBalanceNotes().isBlank()) {
            String notes = payment.getCustomerBalanceNotes();
            if (notes.length() > 400) {
                notes = notes.substring(0, 400) + "…";
            }
            document.add(new Paragraph("Customer balance notes: " + notes.replace("\n", " | ")));
        }

        document.close();

        return out.toByteArray();
    }

    private static String safe(Object value) {
        return value != null ? value.toString() : "";
    }
}