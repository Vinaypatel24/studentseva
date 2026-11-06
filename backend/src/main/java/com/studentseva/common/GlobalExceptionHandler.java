package com.studentseva.common;

import com.studentseva.common.exceptions.ForbiddenException;
import com.studentseva.common.exceptions.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global error handler for the entire application.
 *
 * @RestControllerAdvice means:
 *   "Watch ALL controllers. If any of them throw an exception,
 *    intercept it here and return a clean JSON response instead of
 *    Spring's default ugly HTML error page."
 *
 * Without this, when an exception is thrown, the user would see:
 *   500 Internal Server Error — white label error page (ugly, exposes internals)
 *
 * With this, every error becomes:
 *   {
 *     "status": 404,
 *     "error": "NOT_FOUND",
 *     "message": "User not found with email: vinay@gmail.com",
 *     "timestamp": "2026-11-06T10:25:00Z"
 *   }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 Not Found ─────────────────────────────────────────────────────────

    /**
     * Handles: throw new NotFoundException("User not found")
     * Returns: HTTP 404 with JSON body
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    // ── 403 Forbidden ─────────────────────────────────────────────────────────

    /**
     * Handles: throw new ForbiddenException("You cannot edit this listing")
     * Returns: HTTP 403 with JSON body
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage());
    }

    // ── 400 Validation Failed (@Valid on request body fields) ─────────────────

    /**
     * Handles: @Valid annotation failures on @RequestBody objects.
     *
     * Example: If a request body has @NotBlank on "email" and user sends
     * email = "", Spring throws MethodArgumentNotValidException.
     *
     * We collect all field errors and return them together:
     *   { "status": 400, "error": "VALIDATION_FAILED",
     *     "message": "email: must not be blank; name: must not be blank" }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        // Collect all field-level errors into one readable string
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", errors);
    }

    // ── 400 Constraint Violation (@Validated on method params) ────────────────

    /**
     * Handles: @Validated + @NotBlank on service/controller method parameters.
     * Similar to above but for path variables and query params.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String errors = ex.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));

        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", errors);
    }

    // ── 500 Catch-All (unexpected errors) ─────────────────────────────────────

    /**
     * Catches ANY exception not handled above.
     *
     * IMPORTANT: We return a generic message to the client — NEVER expose
     * the actual exception message in production (it can leak internal details).
     * The real error is logged server-side for the developer to debug.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        // Log the real error on the server (developer can see it in logs)
        // In a full app we'd use: log.error("Unexpected error", ex);
        ex.printStackTrace();

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later."
        );
    }

    // ── Helper: builds the standard error response map ────────────────────────

    /**
     * Builds a consistent error response body:
     * {
     *   "status": 404,
     *   "error": "NOT_FOUND",
     *   "message": "User not found",
     *   "timestamp": "2026-11-06T10:25:00Z"
     * }
     */
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String error, String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());       // numeric code: 404
        body.put("error", error);                 // string code: "NOT_FOUND"
        body.put("message", message);             // human readable
        body.put("timestamp", Instant.now());     // when did it happen

        return ResponseEntity.status(status).body(body);
    }
}

