package com.studentseva.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload to request an OTP via email.
 */
public record SendOtpRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a valid email address")
        String email,

        String role // Optional: "STUDENT" or "VENDOR" (defaults to STUDENT if new user)
) {
}

