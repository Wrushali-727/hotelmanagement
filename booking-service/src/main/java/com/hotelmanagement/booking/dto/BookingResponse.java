package com.hotelmanagement.booking.dto;

import com.hotelmanagement.booking.entity.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID userId,
        UUID roomId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BigDecimal totalPrice,
        BookingStatus status,
        Instant createdAt
) {
}
