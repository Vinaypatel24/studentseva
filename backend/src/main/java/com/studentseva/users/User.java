package com.studentseva.users;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity that maps to the 'users' table in PostgreSQL (Supabase).
 *
 * It also implements UserDetails — Spring Security's interface that tells
 * the security framework "this object IS the logged-in user".
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

    // ── Primary Key ──────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ── Core Fields ──────────────────────────────────────────────────────────

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name")
    private String name;

    /**
     * Role controls what the user can do.
     * STUDENT  → browse, follow, review
     * VENDOR   → manage own business listing
     * ADMIN    → manage everything
     *
     * Stored as a plain string in the DB column "role".
     * @Enumerated(STRING) tells JPA: save "STUDENT" not "0".
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    /**
     * Account lifecycle status.
     * PENDING  → registered but not yet verified (first OTP not done)
     * ACTIVE   → normal, can log in
     * SUSPENDED → banned by admin
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    // ── Timestamps ───────────────────────────────────────────────────────────

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── Lifecycle Hooks ──────────────────────────────────────────────────────

    /**
     * Called by JPA automatically before INSERT.
     * Sets default values so we never accidentally insert nulls.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.role == null)   this.role   = Role.STUDENT;
        if (this.status == null) this.status = Status.ACTIVE;
        if (this.name == null || this.name.isBlank()) {
            this.name = (this.email != null && this.email.contains("@"))
                    ? this.email.substring(0, this.email.indexOf('@'))
                    : "Student";
        }
    }

    /**
     * Called by JPA automatically before UPDATE.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // ── Enums (defined inside the class for simplicity) ───────────────────────

    public enum Role {
        STUDENT, VENDOR, ADMIN
    }

    public enum Status {
        ACTIVE, SUSPENDED, DELETED
    }

    // ── UserDetails Implementation ────────────────────────────────────────────
    //
    // Spring Security calls these methods to decide:
    //   1. What roles does this user have?  → getAuthorities()
    //   2. Is the account usable?           → isEnabled(), isAccountNonLocked()
    //   3. What is the "username"?          → getUsername() — we use email
    //
    // We use email as the unique identifier (not a separate username field).

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // "ROLE_" prefix is required by Spring Security for role-based checks
        // e.g., role ADMIN → authority "ROLE_ADMIN"
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        // We use OTP-based login — no password stored. Return null.
        return null;
    }

    @Override
    public String getUsername() {
        // Spring Security uses "username" as the unique key.
        // For us, the unique key is email.
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // We don't expire accounts
    }

    @Override
    public boolean isAccountNonLocked() {
        // A SUSPENDED user is locked out
        return status != Status.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credentials don't expire (OTP-based)
    }

    @Override
    public boolean isEnabled() {
        // Only ACTIVE users can use the app
        return status == Status.ACTIVE;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId()                   { return id; }
    public void setId(UUID id)            { this.id = id; }

    public String getEmail()              { return email; }
    public void setEmail(String email)    { this.email = email; }

    public String getName()               { return name; }
    public void setName(String name)      { this.name = name; }

    public Role getRole()                 { return role; }
    public void setRole(Role role)        { this.role = role; }

    public Status getStatus()             { return status; }
    public void setStatus(Status status)  { this.status = status; }

    public Instant getCreatedAt()         { return createdAt; }
    public Instant getUpdatedAt()         { return updatedAt; }
}

