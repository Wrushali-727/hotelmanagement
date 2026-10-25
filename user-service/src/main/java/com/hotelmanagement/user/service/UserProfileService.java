package com.hotelmanagement.user.service;

import com.hotelmanagement.user.dto.UserProfileRequest;
import com.hotelmanagement.user.dto.UserProfileResponse;
import com.hotelmanagement.user.entity.UserProfile;
import com.hotelmanagement.user.exception.ResourceNotFoundException;
import com.hotelmanagement.user.repository.UserProfileRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {
    private final UserProfileRepository repository;

    public UserProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UserProfileResponse upsert(UUID keycloakUserId, UserProfileRequest request) {
        UserProfile profile = repository.findByKeycloakUserId(keycloakUserId).orElseGet(UserProfile::new);
        profile.setKeycloakUserId(keycloakUserId);
        profile.setEmail(request.email());
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhoneNumber(request.phoneNumber());
        profile.setRole(request.role());
        return toResponse(repository.save(profile));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getMe(UUID keycloakUserId) {
        return repository.findByKeycloakUserId(keycloakUserId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getKeycloakUserId(),
                profile.getEmail(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhoneNumber(),
                profile.getRole(),
                profile.getCreatedAt(),
                profile.getUpdatedAt());
    }
}
