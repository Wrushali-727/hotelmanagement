package com.hotelmanagement.hotel.service;

import com.hotelmanagement.hotel.dto.RoomRequest;
import com.hotelmanagement.hotel.dto.RoomResponse;
import com.hotelmanagement.hotel.entity.Hotel;
import com.hotelmanagement.hotel.entity.Room;
import com.hotelmanagement.hotel.exception.ResourceNotFoundException;
import com.hotelmanagement.hotel.repository.RoomRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {
    private final RoomRepository repository;
    private final HotelService hotelService;

    public RoomService(RoomRepository repository, HotelService hotelService) {
        this.repository = repository;
        this.hotelService = hotelService;
    }

    @Transactional
    public RoomResponse create(UUID hotelId, RoomRequest request) {
        Hotel hotel = hotelService.findEntity(hotelId);
        Room room = new Room();
        room.setHotel(hotel);
        apply(room, request);
        return toResponse(repository.save(room));
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> findByHotel(UUID hotelId, boolean availableOnly) {
        List<Room> rooms = availableOnly
                ? repository.findByHotelIdAndAvailableTrue(hotelId)
                : repository.findByHotelId(hotelId);
        return rooms.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse get(UUID id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    public RoomResponse update(UUID id, RoomRequest request) {
        Room room = findEntity(id);
        apply(room, request);
        return toResponse(room);
    }

    @Transactional
    public RoomResponse setAvailability(UUID id, boolean available) {
        Room room = findEntity(id);
        room.setAvailable(available);
        return toResponse(room);
    }

    private Room findEntity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    private void apply(Room room, RoomRequest request) {
        room.setRoomNumber(request.roomNumber());
        room.setRoomType(request.roomType());
        room.setPricePerNight(request.pricePerNight());
        room.setAvailable(request.available());
    }

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getHotel().getId(),
                room.getRoomNumber(),
                room.getRoomType(),
                room.getPricePerNight(),
                room.isAvailable(),
                room.getVersion());
    }
}
