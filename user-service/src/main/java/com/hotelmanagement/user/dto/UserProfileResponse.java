package com.hotelmanagement.user.dto;

import com.hotelmanagement.user.entity.UserRole;
import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        UUID keycloakUserId,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        UserRole role,
        Instant createdAt,
        Instant updatedAt
) {
}
