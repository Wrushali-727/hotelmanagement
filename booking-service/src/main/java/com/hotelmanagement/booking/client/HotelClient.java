package com.hotelmanagement.booking.client;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HotelClient {
    private final RestTemplate restTemplate;
    private final String hotelServiceUrl;

    public HotelClient(RestTemplate restTemplate, @Value("${services.hotel-service.url}") String hotelServiceUrl) {
        this.restTemplate = restTemplate;
        this.hotelServiceUrl = hotelServiceUrl;
    }

    public RoomResponse getRoom(UUID roomId) {
        return restTemplate.getForObject(hotelServiceUrl + "/api/rooms/{id}", RoomResponse.class, roomId);
    }

    public record RoomResponse(
            UUID id,
            UUID hotelId,
            String roomNumber,
            String roomType,
            BigDecimal pricePerNight,
            boolean available,
            long version
    ) {
    }
}
