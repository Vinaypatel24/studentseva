package com.studentseva.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource does not exist in the database.
 *
 * Examples:
 *   - User ID not found
 *   - Business listing not found
 *   - Offering not found
 *
 * @ResponseStatus(404) tells Spring: if this exception reaches the
 * web layer unhandled, automatically send HTTP 404.
 * Our GlobalExceptionHandler catches it first and sends a clean JSON response.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    /**
     * @param message Human-readable description of what was not found.
     *                Example: "User not found with email: vinay@gmail.com"
     */
    public NotFoundException(String message) {
        super(message);
    }
}

