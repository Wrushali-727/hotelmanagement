package com.hotelmanagement.hotel.repository;

import com.hotelmanagement.hotel.entity.Room;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findByHotelId(UUID hotelId);

    List<Room> findByHotelIdAndAvailableTrue(UUID hotelId);
}
