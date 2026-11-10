package com.studentseva.businesses.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for nearby spatial search.
 * Contains full business details plus the geodesic distance in meters from the user's location.
 */
public record NearbyBusinessResponse(
        UUID id,
        UUID ownerId,
        String ownerName,
        String name,
        String category,
        String description,
        String address,
        Double latitude,
        Double longitude,
        String phone,
        String whatsapp,
        String status,
        String verificationStatus,
        Double distanceMeters,
        Instant createdAt
) {
}

