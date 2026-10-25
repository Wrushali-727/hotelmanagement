package com.hotelmanagement.hotel.service;

import com.hotelmanagement.hotel.dto.HotelRequest;
import com.hotelmanagement.hotel.dto.HotelResponse;
import com.hotelmanagement.hotel.entity.Hotel;
import com.hotelmanagement.hotel.exception.ResourceNotFoundException;
import com.hotelmanagement.hotel.repository.HotelRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HotelService {
    private final HotelRepository repository;

    public HotelService(HotelRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public HotelResponse create(HotelRequest request) {
        Hotel hotel = new Hotel();
        apply(hotel, request);
        return toResponse(repository.save(hotel));
    }

    @Transactional(readOnly = true)
    public List<HotelResponse> findAll(String city) {
        List<Hotel> hotels = city == null || city.isBlank()
                ? repository.findAll()
                : repository.findByCityIgnoreCaseContaining(city);
        return hotels.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public HotelResponse get(UUID id) {
        return toResponse(findEntity(id));
    }

    @Transactional
    public HotelResponse update(UUID id, HotelRequest request) {
        Hotel hotel = findEntity(id);
        apply(hotel, request);
        return toResponse(hotel);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(findEntity(id));
    }

    Hotel findEntity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
    }

    private void apply(Hotel hotel, HotelRequest request) {
        hotel.setName(request.name());
        hotel.setDescription(request.description());
        hotel.setCity(request.city());
        hotel.setAddress(request.address());
        hotel.setRating(request.rating());
    }

    private HotelResponse toResponse(Hotel hotel) {
        return new HotelResponse(hotel.getId(), hotel.getName(), hotel.getDescription(), hotel.getCity(), hotel.getAddress(), hotel.getRating());
    }
}
