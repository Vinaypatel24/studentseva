package com.studentseva.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user is authenticated (logged in) but tries to access
 * a resource they are NOT allowed to access.
 *
 * Examples:
 *   - A STUDENT tries to edit a business listing they don't own
 *   - A VENDOR tries to access /api/v1/admin/* routes
 *
 * Note the difference:
 *   401 Unauthorized  → Not logged in at all  (handled by Spring Security)
 *   403 Forbidden     → Logged in, but no permission  (this exception)
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    /**
     * @param message Human-readable description of why access was denied.
     *                Example: "You do not have permission to edit this listing"
     */
    public ForbiddenException(String message) {
        super(message);
    }
}

