package com.hotelmanagement.booking.controller;

import com.hotelmanagement.booking.dto.BookingCreateRequest;
import com.hotelmanagement.booking.dto.BookingResponse;
import com.hotelmanagement.booking.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('GUEST')")
    BookingResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody BookingCreateRequest request) {
        return service.create(UUID.fromString(jwt.getSubject()), request);
    }

    @GetMapping("/me")
    BookingResponseList mine(@AuthenticationPrincipal Jwt jwt) {
        return new BookingResponseList(service.myBookings(UUID.fromString(jwt.getSubject())));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_STAFF')")
    BookingResponseList all() {
        return new BookingResponseList(service.allBookings());
    }

    @GetMapping("/{id}")
    BookingResponse get(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return service.get(id, UUID.fromString(jwt.getSubject()), privileged(authentication));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_STAFF')")
    BookingResponse confirm(@PathVariable UUID id) {
        return service.confirm(id);
    }

    @PatchMapping("/{id}/cancel")
    BookingResponse cancel(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return service.cancel(id, UUID.fromString(jwt.getSubject()), privileged(authentication));
    }

    private boolean privileged(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_HOTEL_STAFF"));
    }

    public record BookingResponseList(List<BookingResponse> bookings) {
    }
}
