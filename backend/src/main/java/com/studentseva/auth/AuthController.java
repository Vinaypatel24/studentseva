package com.studentseva.auth;

import com.studentseva.auth.dto.AuthResponse;
import com.studentseva.auth.dto.SendOtpRequest;
import com.studentseva.auth.dto.VerifyOtpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication REST controller for StudentSeva.
 * Handles Email OTP issuance, verification, session check, and logout.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for passwordless email OTP authentication and session management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Request a 6-digit OTP sent to email")
    public ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendOtp(request);
        return ResponseEntity.ok(Map.of(
                "message", "Verification code sent to " + request.email()
        ));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify 6-digit OTP and receive HttpOnly JWT cookie")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthService.AuthResult result = authService.verifyOtp(request);
        ResponseCookie cookie = authService.createAuthCookie(result.token());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authService.toAuthResponse(result.user()));
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user's profile")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out user and clear HttpOnly JWT cookie")
    public ResponseEntity<Map<String, String>> logout() {
        ResponseCookie cookie = authService.createLogoutCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }
}

