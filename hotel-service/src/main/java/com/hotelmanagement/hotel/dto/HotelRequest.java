package com.hotelmanagement.hotel.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record HotelRequest(
        @NotBlank String name,
        String description,
        @NotBlank String city,
        @NotBlank String address,
        @NotNull @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal rating
) {
}
