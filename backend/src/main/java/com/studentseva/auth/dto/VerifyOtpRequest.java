package com.studentseva.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request payload to verify the 6-digit OTP.
 */
public record VerifyOtpRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a valid email address")
        String email,

        @NotBlank(message = "OTP must not be blank")
        @Pattern(regexp = "^\\d{6}$", message = "OTP must be exactly 6 digits")
        String otp,

        String role, // Optional: "STUDENT" or "VENDOR" (defaults to STUDENT)
        String name  // Optional: user's display name
) {
}
