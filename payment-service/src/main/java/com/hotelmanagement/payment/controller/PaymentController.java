package com.hotelmanagement.payment.controller;

import com.hotelmanagement.payment.dto.PaymentIntentRequest;
import com.hotelmanagement.payment.dto.PaymentResponse;
import com.hotelmanagement.payment.service.PaymentService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping("/intents")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('GUEST')")
    PaymentResponse createIntent(@Valid @RequestBody PaymentIntentRequest request) throws StripeException {
        return service.createPaymentIntent(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_STAFF')")
    PaymentResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_STAFF')")
    List<PaymentResponse> byBooking(@PathVariable UUID bookingId) {
        return service.findByBooking(bookingId);
    }

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void webhook(@RequestBody String payload, @RequestHeader(name = "Stripe-Signature", required = false) String signature)
            throws StripeException {
        service.handleWebhook(payload, signature);
    }
}
