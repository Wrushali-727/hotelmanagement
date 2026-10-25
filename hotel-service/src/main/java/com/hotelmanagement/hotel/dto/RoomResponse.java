package com.hotelmanagement.hotel.dto;

import com.hotelmanagement.hotel.entity.RoomType;
import java.math.BigDecimal;
import java.util.UUID;

public record RoomResponse(
        UUID id,
        UUID hotelId,
        String roomNumber,
        RoomType roomType,
        BigDecimal pricePerNight,
        boolean available,
        long version
) {
}
