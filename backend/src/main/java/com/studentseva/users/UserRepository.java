package com.studentseva.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for the User entity.
 *
 * JpaRepository gives us these methods FOR FREE (no code needed):
 *   save(user)         → INSERT or UPDATE
 *   findById(uuid)     → SELECT by primary key
 *   findAll()          → SELECT all users
 *   delete(user)       → DELETE
 *   count()            → SELECT COUNT(*)
 *   existsById(uuid)   → SELECT EXISTS(...)
 *
 * We only need to write queries that JpaRepository doesn't already provide.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by their email address.
     *
     * Spring Data JPA auto-generates the SQL from the method name:
     *   findBy  → SELECT * FROM users WHERE
     *   Email   → email = ?
     *
     * Returns Optional<User> so the caller must handle the "not found" case
     * explicitly — no accidental NullPointerExceptions.
     *
     * Generated SQL: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with this email already exists.
     * Used during registration to avoid duplicate accounts.
     *
     * Generated SQL: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);
}

