package com.hotelmanagement.booking.service;

import com.hotelmanagement.booking.client.HotelClient;
import com.hotelmanagement.booking.dto.BookingCreateRequest;
import com.hotelmanagement.booking.dto.BookingResponse;
import com.hotelmanagement.booking.entity.Booking;
import com.hotelmanagement.booking.entity.BookingStatus;
import com.hotelmanagement.booking.exception.ResourceNotFoundException;
import com.hotelmanagement.booking.repository.BookingRepository;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private static final List<BookingStatus> ACTIVE_STATUSES = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository repository;
    private final HotelClient hotelClient;

    public BookingService(BookingRepository repository, HotelClient hotelClient) {
        this.repository = repository;
        this.hotelClient = hotelClient;
    }

    @Transactional
    public BookingResponse create(UUID userId, BookingCreateRequest request) {
        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        HotelClient.RoomResponse room = hotelClient.getRoom(request.roomId());
        if (room == null) {
            throw new ResourceNotFoundException("Room not found");
        }
        if (!room.available()) {
            throw new IllegalArgumentException("Room is currently unavailable");
        }
        if (repository.existsActiveOverlap(request.roomId(), request.checkInDate(), request.checkOutDate(), ACTIVE_STATUSES)) {
            throw new IllegalArgumentException("Room is already booked for the requested date range");
        }

        long nights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setRoomId(request.roomId());
        booking.setCheckInDate(request.checkInDate());
        booking.setCheckOutDate(request.checkOutDate());
        booking.setTotalPrice(room.pricePerNight().multiply(BigDecimal.valueOf(nights)));
        booking.setStatus(BookingStatus.PENDING);
        return toResponse(repository.saveAndFlush(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> myBookings(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> allBookings() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse get(UUID id, UUID requesterId, boolean privileged) {
        Booking booking = findEntity(id);
        if (!privileged && !booking.getUserId().equals(requesterId)) {
            throw new ResourceNotFoundException("Booking not found");
        }
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse confirm(UUID id) {
        Booking booking = findEntity(id);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled booking cannot be confirmed");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse cancel(UUID id, UUID requesterId, boolean privileged) {
        Booking booking = findEntity(id);
        if (!privileged && !booking.getUserId().equals(requesterId)) {
            throw new ResourceNotFoundException("Booking not found");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(booking);
    }

    private Booking findEntity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getRoomId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getCreatedAt());
    }
}
