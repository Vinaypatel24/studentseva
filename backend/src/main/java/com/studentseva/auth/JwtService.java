package com.studentseva.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Service for creating and validating JSON Web Tokens (JWT).
 *
 * ── How JWT works in StudentSeva ──
 *
 * 1. User verifies their email OTP  →  AuthService calls generateToken(user)
 * 2. The JWT is placed in an httpOnly cookie named "nearbuzz_token"
 * 3. Every subsequent request carries the cookie automatically (browser does this)
 * 4. JwtAuthenticationFilter reads the cookie, calls this service to validate
 * 5. If valid → user is authenticated. If invalid/expired → 401.
 *
 * ── JWT Structure (3 parts separated by dots) ──
 *
 *   eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ2aW5heUBnbWFpbC5jb20iLCJyb2xlIjoiU1RVREVOV.signature
 *   ───────── HEADER ──────  ──────────── PAYLOAD (claims) ────────────────────  ── SIG ──
 *
 *   Header:  algorithm (HS256)
 *   Payload: subject (email), role, userId, issuedAt, expiration
 *   Signature: HMAC-SHA256 of header+payload using our secret key
 *
 * The signature ensures nobody can tamper with the payload — if even
 * one character changes, the signature won't match and we reject it.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;
    private final String cookieName;

    /**
     * Constructor — Spring injects the values from application.yml.
     *
     * We convert the string secret into a proper cryptographic SecretKey object.
     * Keys.hmacShaKeyFor() ensures the key is the right length for HS256 (>=256 bits).
     */
    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.cookie-name}") String cookieName) {

        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.cookieName = cookieName;
    }

    // ── Token Generation ──────────────────────────────────────────────────────

    /**
     * Creates a signed JWT for the given user.
     *
     * Claims stored inside the token:
     *   sub   → email (the "subject" — who this token belongs to)
     *   role  → STUDENT / VENDOR / ADMIN
     *   uid   → UUID of the user (useful for DB lookups without querying by email)
     *   iat   → issued-at timestamp
     *   exp   → expiration timestamp (24 hours from now)
     *
     * @param email  the user's email address
     * @param role   the user's role (e.g., "STUDENT")
     * @param userId the user's UUID
     * @return signed JWT string
     */
    public String generateToken(String email, String role, UUID userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)                           // "sub" claim
                .claim("role", role)                      // custom claim
                .claim("uid", userId.toString())          // custom claim
                .issuedAt(now)                            // "iat" claim
                .expiration(expiry)                       // "exp" claim
                .signWith(signingKey)                     // sign with HS256
                .compact();                               // serialize to string
    }

    // ── Token Validation ──────────────────────────────────────────────────────

    /**
     * Validates the JWT and checks that the subject (email) matches the
     * given UserDetails. Returns true only if:
     *   1. Signature is valid (not tampered with)
     *   2. Token is not expired
     *   3. Subject matches the UserDetails username (email)
     *
     * @param token       the JWT string from the cookie
     * @param userDetails the user loaded from the database
     * @return true if the token is valid for this user
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            // Any JJWT parsing/signature error → token is invalid
            return false;
        }
    }

    // ── Claim Extraction ──────────────────────────────────────────────────────

    /**
     * Extracts the email (subject claim) from the JWT.
     * Throws JwtException if the token is malformed or the signature is invalid.
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts the user's role from the custom "role" claim.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Extracts the user's UUID from the custom "uid" claim.
     */
    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).get("uid", String.class));
    }

    /**
     * Returns the cookie name configured in application.yml ("nearbuzz_token").
     * Used by JwtAuthenticationFilter to know which cookie to look for.
     */
    public String getCookieName() {
        return cookieName;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Parses the JWT and verifies the signature in one step.
     *
     * Jwts.parser()
     *   .verifyWith(signingKey)   → "use this key to check the signature"
     *   .build()                  → create the parser
     *   .parseSignedClaims(token) → parse AND verify (throws if invalid)
     *   .getPayload()             → return the claims (payload data)
     *
     * If the signature doesn't match or the token is malformed,
     * JJWT throws a JwtException automatically.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks if the token's expiration date is in the past.
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}

