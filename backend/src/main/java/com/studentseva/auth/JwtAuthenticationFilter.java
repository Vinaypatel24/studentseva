package com.studentseva.auth;

import com.studentseva.users.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * JWT Authentication Filter — the gatekeeper of every HTTP request.
 *
 * ── Where this sits in the request lifecycle ──
 *
 *   Browser sends request with "nearbuzz_token" cookie
 *       ↓
 *   [JwtAuthenticationFilter]  ← YOU ARE HERE
 *       ↓
 *   Spring Security authorization checks (hasRole, authenticated, etc.)
 *       ↓
 *   Controller method
 *       ↓
 *   Response back to browser
 *
 * ── What this filter does on EVERY request ──
 *
 *   1. Look for a cookie named "nearbuzz_token"
 *   2. If no cookie → skip (let Spring Security handle it — will 401 if route is protected)
 *   3. If cookie found → extract the JWT string
 *   4. Parse the JWT, extract the email from it
 *   5. Load the User from the database by email
 *   6. Validate the token (signature + expiry + email match)
 *   7. If valid → tell Spring Security "this user is authenticated"
 *   8. Continue to the next filter / controller
 *
 * ── Why OncePerRequestFilter? ──
 *
 *   Guarantees this filter runs exactly once per request, even if the request
 *   gets forwarded internally (e.g., error handling forwards). Without it,
 *   a forwarded request could run the filter twice, wasting a DB query.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    /**
     * Constructor injection — Spring automatically provides both beans.
     * We don't use @Autowired on fields; constructor injection is the
     * recommended pattern because:
     *   - Dependencies are explicit (you can see them in the constructor)
     *   - The object is fully initialized after construction
     *   - Easier to test (pass mocks directly)
     */
    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ── Step 1: Extract JWT from cookie ───────────────────────────────────

        String token = extractTokenFromCookie(request);

        // No cookie found → skip this filter, let the request continue.
        // Spring Security will decide whether to allow or block it
        // based on the route rules in SecurityConfig.
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // ── Step 2: Extract email from JWT ────────────────────────────────────

        String email;
        try {
            email = jwtService.extractEmail(token);
        } catch (JwtException e) {
            // Token is malformed, tampered, or has an invalid signature.
            // Clear the cookie so the browser doesn't keep sending a bad token.
            clearCookie(response);
            filterChain.doFilter(request, response);
            return;
        }

        // ── Step 3: Only authenticate if not already authenticated ────────────
        //
        // SecurityContextHolder.getContext().getAuthentication() == null means
        // no filter before this one has already authenticated the user.
        // This prevents redundant DB queries if somehow the user is
        // already authenticated in this request.

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // ── Step 4: Load user from database ───────────────────────────────

            UserDetails userDetails = userRepository.findByEmail(email).orElse(null);

            // User not found in DB (maybe deleted after the token was issued)
            if (userDetails == null) {
                clearCookie(response);
                filterChain.doFilter(request, response);
                return;
            }

            // ── Step 5: Validate the token against the user ───────────────────

            if (jwtService.isTokenValid(token, userDetails)) {

                // ── Step 6: Tell Spring Security "this user is authenticated" ─
                //
                // UsernamePasswordAuthenticationToken is Spring's way of saying
                // "here is an authenticated user with these authorities".
                //
                // Parameters:
                //   1. userDetails   → the principal (the "who")
                //   2. null          → credentials (null because we already verified via JWT)
                //   3. authorities   → roles/permissions (ROLE_STUDENT, etc.)

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Attach request details (IP address, session ID) for audit logging
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Set the authentication in the security context.
                // From this point on, Spring Security knows who the user is.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                // Token is expired or email mismatch — clear the bad cookie
                clearCookie(response);
            }
        }

        // ── Step 7: Continue the filter chain ─────────────────────────────────
        // Whether authenticated or not, we always call doFilter() to let
        // the request proceed. Spring Security's authorization layer will
        // block unauthenticated requests to protected routes.
        filterChain.doFilter(request, response);
    }

    // ── Helper: extract JWT string from cookies ───────────────────────────────

    /**
     * Searches the request's cookies for "nearbuzz_token" and returns its value.
     * Returns null if no cookies exist or the cookie is not found.
     */
    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> jwtService.getCookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    // ── Helper: clear bad cookie ──────────────────────────────────────────────

    /**
     * Sends back a Set-Cookie header that tells the browser to delete
     * the "nearbuzz_token" cookie by setting maxAge to 0.
     *
     * This prevents the browser from sending an invalid/expired token
     * on every subsequent request (which would waste a DB query each time).
     */
    private void clearCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(jwtService.getCookieName(), "");
        cookie.setMaxAge(0);       // tells browser to delete immediately
        cookie.setPath("/");       // must match the path the cookie was set with
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}

