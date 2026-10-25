package com.hotelmanagement.hotel.dto;

import com.hotelmanagement.hotel.entity.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RoomRequest(
        @NotBlank String roomNumber,
        @NotNull RoomType roomType,
        @NotNull @DecimalMin("0.01") BigDecimal pricePerNight,
        boolean available
) {
}
