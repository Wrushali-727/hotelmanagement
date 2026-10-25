package com.hotelmanagement.payment.dto;

import com.hotelmanagement.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID bookingId,
        String stripePaymentIntentId,
        String idempotencyKey,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        Instant createdAt,
        String clientSecret
) {
}
