package com.studentseva.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Data repository for OTP records.
 */
@Repository
public interface OtpRepository extends JpaRepository<OtpRequest, UUID> {

    /**
     * Finds the most recently issued unused OTP for this email.
     */
    Optional<OtpRequest> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    /**
     * Counts how many OTPs were requested by this email in a given time window.
     * Used for rate limiting to prevent spamming/abuse.
     */
    long countByEmailAndCreatedAtAfter(String email, Instant after);
}

