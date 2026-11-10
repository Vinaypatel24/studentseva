package com.studentseva.businesses.dto;

import com.studentseva.businesses.Business;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload to create or register a business listing.
 */
public record CreateBusinessRequest(
        @NotBlank(message = "Business name is required")
        @Size(max = 300, message = "Business name cannot exceed 300 characters")
        String name,

        @NotNull(message = "Category is required")
        Business.Category category,

        String description,

        @NotBlank(message = "Address is required")
        String address,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
        Double latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
        Double longitude,

        String phone,

        String whatsapp
) {
}

