package com.hotelmanagement.hotel.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record HotelResponse(
        UUID id,
        String name,
        String description,
        String city,
        String address,
        BigDecimal rating
) {
}
