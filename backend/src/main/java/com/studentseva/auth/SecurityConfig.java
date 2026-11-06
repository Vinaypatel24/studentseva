package com.studentseva.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Spring Security configuration for StudentSeva.
 *
 * Key design decisions:
 *  1. STATELESS sessions — No server-side session stored. JWT cookie is the
 *     only proof of identity. Each request must carry the cookie.
 *
 *  2. CSRF disabled — Safe because we use httpOnly cookies + CORS restriction,
 *     not form-based login. Modern SPA + JWT pattern doesn't need CSRF tokens.
 *
 *  3. JWT filter — JwtAuthenticationFilter runs BEFORE Spring's default
 *     UsernamePasswordAuthenticationFilter. It reads the cookie, validates
 *     the JWT, and sets the SecurityContext so Spring knows who the user is.
 *
 *  4. CORS configured — Only our frontend URL is allowed to call the API.
 *     withCredentials: true is needed so the browser sends the cookie.
 *
 *  5. Exception handling — Custom 401/403 JSON responses instead of Spring's
 *     default HTML error pages.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // allows @PreAuthorize("hasRole('ADMIN')") on controller methods
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    /**
     * The frontend URL (e.g. http://localhost:5173 in dev, https://studentseva.vercel.app in prod).
     * Read from the FRONTEND_URL environment variable.
     */
    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Constructor injection for the JWT filter and ObjectMapper.
     * ObjectMapper is provided by Spring Boot automatically (Jackson auto-config).
     * We use it to serialize error responses to JSON.
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── CSRF: Disabled (safe for stateless JWT + CORS setup) ──────────
            .csrf(csrf -> csrf.disable())

            // ── Session: Stateless (no HttpSession, JWT is the state) ─────────
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── CORS: Apply our corsConfigurationSource bean ──────────────────
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── Route Permissions ─────────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth

                // Public routes — no login required
                .requestMatchers("/api/v1/health/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                // Admin-only routes — ROLE_ADMIN required
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // Everything else requires the user to be authenticated
                .anyRequest().authenticated()
            )

            // ── Exception Handling ────────────────────────────────────────────
            //
            // These handlers catch security errors BEFORE they reach any
            // controller, so GlobalExceptionHandler never sees them.
            //
            // authenticationEntryPoint → fires when an unauthenticated user
            //   tries to access a protected route (HTTP 401)
            //
            // accessDeniedHandler → fires when an authenticated user tries
            //   to access a route they don't have permission for (HTTP 403)
            //   e.g., a STUDENT hitting /api/v1/admin/something
            //
            .exceptionHandling(ex -> ex

                // 401 — Not logged in
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setStatus(401);
                    response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                            "status", 401,
                            "error", "UNAUTHORIZED",
                            "message", "Please log in to access this resource",
                            "timestamp", Instant.now().toString()
                    )));
                })

                // 403 — Logged in but wrong role
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json");
                    response.setStatus(403);
                    response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                            "status", 403,
                            "error", "FORBIDDEN",
                            "message", "You don't have permission to access this resource",
                            "timestamp", Instant.now().toString()
                    )));
                })
            )

            // ── JWT Filter: Run BEFORE Spring's default auth filter ───────────
            //
            // addFilterBefore means:
            //   "Run JwtAuthenticationFilter first, THEN UsernamePasswordAuthenticationFilter"
            //
            // Why before? Because our filter sets the SecurityContext with the
            // authenticated user. By the time Spring's default filter runs,
            // the user is already authenticated and it has nothing to do.
            //
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS = Cross-Origin Resource Sharing.
     *
     * By default, browsers BLOCK JavaScript on one domain from calling an API
     * on a different domain. CORS is the mechanism that lets the API say:
     * "I trust requests from THIS specific origin — let them through."
     *
     * Our setup:
     *   - Only our React frontend URL is allowed
     *   - allowCredentials = true  → browser will send the httpOnly cookie
     *   - allowedMethods = GET, POST, PUT, DELETE, PATCH
     *   - allowedHeaders = *      → accept any request headers
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Only allow our frontend to call this API
        config.setAllowedOrigins(List.of(frontendUrl));

        // Allow the browser to send cookies (the JWT httpOnly cookie)
        config.setAllowCredentials(true);

        // HTTP methods the frontend is allowed to use
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Allow all request headers (Content-Type, Authorization, etc.)
        config.setAllowedHeaders(List.of("*"));

        // Apply this CORS config to ALL routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
