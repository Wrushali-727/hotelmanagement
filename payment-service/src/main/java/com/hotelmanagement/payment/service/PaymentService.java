package com.hotelmanagement.payment.service;

import com.hotelmanagement.payment.dto.PaymentIntentRequest;
import com.hotelmanagement.payment.dto.PaymentResponse;
import com.hotelmanagement.payment.entity.Payment;
import com.hotelmanagement.payment.entity.PaymentStatus;
import com.hotelmanagement.payment.exception.ResourceNotFoundException;
import com.hotelmanagement.payment.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final PaymentRepository repository;
    private final String stripeApiKey;
    private final String webhookSecret;

    public PaymentService(
            PaymentRepository repository,
            @Value("${stripe.api-key:}") String stripeApiKey,
            @Value("${stripe.webhook-secret:}") String webhookSecret) {
        this.repository = repository;
        this.stripeApiKey = stripeApiKey;
        this.webhookSecret = webhookSecret;
    }

    @Transactional
    public PaymentResponse createPaymentIntent(PaymentIntentRequest request) throws StripeException {
        return repository.findByIdempotencyKey(request.idempotencyKey())
                .map(payment -> toResponse(payment, null))
                .orElseGet(() -> createNewPayment(request));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByBooking(UUID bookingId) {
        return repository.findByBookingId(bookingId).stream()
                .map(payment -> toResponse(payment, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(UUID id) {
        return repository.findById(id)
                .map(payment -> toResponse(payment, null))
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    @Transactional
    public void handleWebhook(String payload, String signatureHeader) throws StripeException {
        Event event = parseEvent(payload, signatureHeader);
        if ("payment_intent.succeeded".equals(event.getType()) || "payment_intent.payment_failed".equals(event.getType())) {
            event.getDataObjectDeserializer().getObject()
                    .filter(PaymentIntent.class::isInstance)
                    .map(PaymentIntent.class::cast)
                    .ifPresent(intent -> updateStatus(intent.getId(), "payment_intent.succeeded".equals(event.getType())
                            ? PaymentStatus.SUCCEEDED
                            : PaymentStatus.FAILED));
        }
    }

    private PaymentResponse createNewPayment(PaymentIntentRequest request) {
        try {
            Payment payment = new Payment();
            payment.setBookingId(request.bookingId());
            payment.setAmount(request.amount());
            payment.setCurrency(request.currency().toLowerCase(Locale.ROOT));
            payment.setIdempotencyKey(request.idempotencyKey());
            payment.setStatus(PaymentStatus.PENDING);

            String clientSecret = null;
            if (stripeApiKey != null && !stripeApiKey.isBlank()) {
                Stripe.apiKey = stripeApiKey;
                PaymentIntent intent = PaymentIntent.create(
                        PaymentIntentCreateParams.builder()
                                .setAmount(toMinorUnits(request.amount()))
                                .setCurrency(request.currency().toLowerCase(Locale.ROOT))
                                .putMetadata("booking_id", request.bookingId().toString())
                                .build(),
                        RequestOptions.builder().setIdempotencyKey(request.idempotencyKey()).build());
                payment.setStripePaymentIntentId(intent.getId());
                clientSecret = intent.getClientSecret();
            } else {
                payment.setStripePaymentIntentId("sim_" + UUID.randomUUID());
            }

            return toResponse(repository.save(payment), clientSecret);
        } catch (StripeException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private Event parseEvent(String payload, String signatureHeader) throws SignatureVerificationException {
        if (webhookSecret != null && !webhookSecret.isBlank()) {
            return Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        }
        return Event.GSON.fromJson(payload, Event.class);
    }

    private void updateStatus(String stripePaymentIntentId, PaymentStatus status) {
        repository.findByStripePaymentIntentId(stripePaymentIntentId).ifPresent(payment -> payment.setStatus(status));
    }

    private long toMinorUnits(BigDecimal amount) {
        return amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private PaymentResponse toResponse(Payment payment, String clientSecret) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBookingId(),
                payment.getStripePaymentIntentId(),
                payment.getIdempotencyKey(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getCreatedAt(),
                clientSecret);
    }
}
