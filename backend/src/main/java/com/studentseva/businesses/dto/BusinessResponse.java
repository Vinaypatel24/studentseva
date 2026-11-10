package com.studentseva.businesses.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Clean response DTO exposing business details with explicit latitude and longitude numbers.
 */
public record BusinessResponse(
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
        Instant createdAt,
        Instant updatedAt
) {
}

