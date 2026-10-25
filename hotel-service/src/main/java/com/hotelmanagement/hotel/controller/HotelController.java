package com.hotelmanagement.hotel.controller;

import com.hotelmanagement.hotel.dto.HotelRequest;
import com.hotelmanagement.hotel.dto.HotelResponse;
import com.hotelmanagement.hotel.dto.RoomRequest;
import com.hotelmanagement.hotel.dto.RoomResponse;
import com.hotelmanagement.hotel.service.HotelService;
import com.hotelmanagement.hotel.service.RoomService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {
    private final HotelService hotelService;
    private final RoomService roomService;

    public HotelController(HotelService hotelService, RoomService roomService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
    }

    @GetMapping
    List<HotelResponse> findAll(@RequestParam(required = false) String city) {
        return hotelService.findAll(city);
    }

    @GetMapping("/{id}")
    HotelResponse get(@PathVariable UUID id) {
        return hotelService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    HotelResponse create(@Valid @RequestBody HotelRequest request) {
        return hotelService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    HotelResponse update(@PathVariable UUID id, @Valid @RequestBody HotelRequest request) {
        return hotelService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    void delete(@PathVariable UUID id) {
        hotelService.delete(id);
    }

    @GetMapping("/{hotelId}/rooms")
    List<RoomResponse> rooms(@PathVariable UUID hotelId, @RequestParam(defaultValue = "false") boolean availableOnly) {
        return roomService.findByHotel(hotelId, availableOnly);
    }

    @PostMapping("/{hotelId}/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_STAFF')")
    RoomResponse createRoom(@PathVariable UUID hotelId, @Valid @RequestBody RoomRequest request) {
        return roomService.create(hotelId, request);
    }

    @PatchMapping("/rooms/{roomId}/availability")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_STAFF')")
    RoomResponse setAvailability(@PathVariable UUID roomId, @RequestParam boolean available) {
        return roomService.setAvailability(roomId, available);
    }
}
