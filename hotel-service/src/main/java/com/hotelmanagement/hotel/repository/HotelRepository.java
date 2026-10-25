package com.hotelmanagement.hotel.repository;

import com.hotelmanagement.hotel.entity.Hotel;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, UUID> {
    List<Hotel> findByCityIgnoreCaseContaining(String city);
}
