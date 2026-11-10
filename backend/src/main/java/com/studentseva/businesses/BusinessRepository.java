package com.studentseva.businesses;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Business operations.
 */
@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {

    /**
     * Finds the business owned by a specific user.
     */
    Optional<Business> findByOwnerId(UUID ownerId);

    /**
     * Checks if a user already owns a business.
     */
    boolean existsByOwnerId(UUID ownerId);

    /**
     * Finds businesses filtered by category.
     */
    List<Business> findByCategory(Business.Category category);
}

