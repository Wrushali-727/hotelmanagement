package com.hotelmanagement.user.controller;

import com.hotelmanagement.user.dto.UserProfileRequest;
import com.hotelmanagement.user.dto.UserProfileResponse;
import com.hotelmanagement.user.service.UserProfileService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {
    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    @GetMapping("/me")
    UserProfileResponse getMe(@AuthenticationPrincipal Jwt jwt) {
        return service.getMe(UUID.fromString(jwt.getSubject()));
    }

    @PutMapping("/me")
    UserProfileResponse upsertMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UserProfileRequest request) {
        return service.upsert(UUID.fromString(jwt.getSubject()), request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    List<UserProfileResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    UserProfileResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }
}
