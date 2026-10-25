package com.hotelmanagement.booking.repository;

import com.hotelmanagement.booking.entity.Booking;
import com.hotelmanagement.booking.entity.BookingStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("""
            select count(b) > 0 from Booking b
            where b.roomId = :roomId
              and b.status in :activeStatuses
              and b.checkInDate < :checkOutDate
              and b.checkOutDate > :checkInDate
            """)
    boolean existsActiveOverlap(
            @Param("roomId") UUID roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("activeStatuses") List<BookingStatus> activeStatuses);
}
