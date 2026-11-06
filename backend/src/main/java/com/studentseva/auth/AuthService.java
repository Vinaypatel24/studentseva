package com.studentseva.auth;

import com.studentseva.auth.dto.AuthResponse;
import com.studentseva.auth.dto.SendOtpRequest;
import com.studentseva.auth.dto.VerifyOtpRequest;
import com.studentseva.common.exceptions.ForbiddenException;
import com.studentseva.common.exceptions.NotFoundException;
import com.studentseva.users.User;
import com.studentseva.users.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service managing passwordless authentication via Email OTP and JWT cookies.
 */
@Service
public class AuthService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${app.otp.max-per-10-minutes:5}")
    private int maxOtpPer10Minutes;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    public AuthService(
            OtpRepository otpRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    /**
     * Generates a 6-digit OTP, stores its BCrypt hash, and emails the plaintext OTP.
     */
    @Transactional
    public void sendOtp(SendOtpRequest request) {
        String email = request.email().trim().toLowerCase();

        // 1. Rate limiting check (e.g. max 5 OTPs within 10 minutes)
        Instant tenMinutesAgo = Instant.now().minus(10, ChronoUnit.MINUTES);
        long recentRequests = otpRepository.countByEmailAndCreatedAtAfter(email, tenMinutesAgo);
        if (recentRequests >= maxOtpPer10Minutes) {
            throw new IllegalStateException("Too many OTP requests. Please wait a few minutes before trying again.");
        }

        // 2. Generate cryptographically strong 6-digit code
        int code = 100000 + secureRandom.nextInt(900000);
        String plainOtp = String.valueOf(code);

        // 3. Hash OTP using BCrypt for database storage
        String otpHash = passwordEncoder.encode(plainOtp);

        // 4. Determine purpose: REGISTER if email doesn't exist, otherwise LOGIN
        OtpRequest.Purpose purpose = userRepository.existsByEmail(email)
                ? OtpRequest.Purpose.LOGIN
                : OtpRequest.Purpose.REGISTER;

        Instant expiresAt = Instant.now().plus(otpExpirationMinutes, ChronoUnit.MINUTES);

        // 5. Persist record
        OtpRequest otpRequest = new OtpRequest(email, otpHash, purpose, expiresAt);
        otpRepository.save(otpRequest);

        // 6. Send email (with dev console fallback)
        emailService.sendOtpEmail(email, plainOtp);
    }

    /**
     * Verifies the submitted OTP, creates or activates the user, and generates a JWT.
     *
     * @return AuthResult containing the User profile and signed JWT token.
     */
    @Transactional
    public AuthResult verifyOtp(VerifyOtpRequest request) {
        String email = request.email().trim().toLowerCase();
        String plainOtp = request.otp().trim();

        // 1. Fetch latest unused OTP for this email
        OtpRequest otpRequest = otpRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("No active OTP request found for this email."));

        // 2. Check if expired
        if (Instant.now().isAfter(otpRequest.getExpiresAt())) {
            throw new IllegalArgumentException("This OTP has expired. Please request a new one.");
        }

        // 3. Verify OTP hash with BCrypt
        if (!passwordEncoder.matches(plainOtp, otpRequest.getOtpHash())) {
            throw new IllegalArgumentException("Invalid OTP verification code.");
        }

        // 4. Invalidate OTP (prevent reuse)
        otpRequest.setUsed(true);
        otpRepository.save(otpRequest);

        // 5. Find existing user or onboard new user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            if (request.name() != null && !request.name().isBlank()) {
                newUser.setName(request.name().trim());
            } else {
                newUser.setName(email.substring(0, email.indexOf('@')));
            }
            if ("VENDOR".equalsIgnoreCase(request.role())) {
                newUser.setRole(User.Role.VENDOR);
            } else {
                newUser.setRole(User.Role.STUDENT);
            }
            newUser.setStatus(User.Status.ACTIVE);
            return userRepository.save(newUser);
        });

        // 6. Check if account is suspended
        if (user.getStatus() == User.Status.SUSPENDED) {
            throw new ForbiddenException("Your account has been suspended. Please contact support.");
        }

        // 7. Mint JWT token
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        return new AuthResult(user, token);
    }

    /**
     * Retrieves the currently authenticated user's profile from the SecurityContext.
     */
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ForbiddenException("Not authenticated");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        return toAuthResponse(user);
    }

    /**
     * Creates an HttpOnly, SameSite=Lax cookie carrying the JWT.
     */
    public ResponseCookie createAuthCookie(String token) {
        return ResponseCookie.from(jwtService.getCookieName(), token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(24 * 60 * 60) // 24 hours
                .sameSite("Lax")
                .build();
    }

    /**
     * Creates a clearing cookie to log the user out.
     */
    public ResponseCookie createLogoutCookie() {
        return ResponseCookie.from(jwtService.getCookieName(), "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0) // delete immediately
                .sameSite("Lax")
                .build();
    }

    public AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getStatus().name()
        );
    }

    /**
     * Internal container holding authenticated User and generated JWT token.
     */
    public record AuthResult(User user, String token) {
    }
}

