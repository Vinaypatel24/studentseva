package com.studentseva.auth.dto;

import java.util.UUID;

/**
 * Clean user profile DTO returned upon successful login / session check.
 */
public record AuthResponse(
        UUID id,
        String email,
        String name,
        String role,
        String status
) {
}

