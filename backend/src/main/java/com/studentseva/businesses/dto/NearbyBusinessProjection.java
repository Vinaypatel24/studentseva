package com.studentseva.businesses.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Spring Data projection interface for native PostGIS spatial query results.
 */
public interface NearbyBusinessProjection {
    UUID getId();
    UUID getOwnerId();
    String getOwnerName();
    String getName();
    String getCategory();
    String getDescription();
    String getAddress();
    Double getLatitude();
    Double getLongitude();
    String getPhone();
    String getWhatsapp();
    String getStatus();
    String getVerificationStatus();
    Double getDistanceMeters();
    Instant getCreatedAt();
}

