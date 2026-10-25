package com.hotelmanagement.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentIntentRequest(
        @NotNull UUID bookingId,
        @NotNull @DecimalMin("0.50") BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String idempotencyKey
) {
}
