package com.cognizant.userservice.config;

import com.cognizant.userservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Custom Spring Security filter for JWT authentication.
 * This filter extends OncePerRequestFilter to ensure it's executed only once per request.
 * It intercepts incoming requests, extracts the JWT, validates it, and sets up
 * the Spring Security authentication context if the token is valid.
 */
@Component // Marks this class as a Spring component, making it discoverable for dependency injection.
@RequiredArgsConstructor // Lombok annotation to generate a constructor with required arguments (final fields).
@Slf4j // Lombok annotation to enable logging via an SLF4J logger named 'log'.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Injected dependency: Service responsible for JWT token creation, validation, and extraction.
    private final JwtService jwtService;
    // Injected dependency: Service to load user-specific data (e.g., from a database).
    private final UserDetailsService userDetailsService;

    /**
     * This core method is executed for every incoming HTTP request that passes through the filter chain.
     * It handles the logic for extracting, validating, and processing the JWT for authentication.
     *
     * @param request The incoming HttpServletRequest.
     * @param response The HttpServletResponse to which the response will be written.
     * @param filterChain The FilterChain to pass the request to the next filter or servlet.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, // Ensures the request object is not null.
            @NonNull HttpServletResponse response, // Ensures the response object is not null.
            @NonNull FilterChain filterChain // Ensures the filterChain object is not null.
    ) throws ServletException, IOException {
        // Log the URI of the incoming request for debugging purposes.
        log.debug("Processing authentication for '{}'", request.getRequestURI());

        // Attempt to retrieve the "Authorization" header from the HTTP request.
        // This header is expected to contain the JWT in the format "Bearer <token>".
        final String authHeader = request.getHeader("Authorization");
        final String jwt; // Variable to hold the extracted JWT string.
        final String userEmail; // Variable to hold the username (typically email) extracted from the JWT.

        // 1. Check for the presence and format of the Authorization header.
        // If the header is missing or doesn't start with "Bearer ", it means
        // either no token is provided or it's not a Bearer token.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Log that no JWT was found and skip authentication for this request.
            log.trace("No JWT token found in request header, skipping JWT authentication.");
            // Continue the filter chain to the next filter/servlet without performing JWT authentication.
            filterChain.doFilter(request, response);
            return; // Exit the method as nothing more to do for this request's JWT processing.
        }

        // 2. Extract the JWT token from the Authorization header.
        // The token starts after "Bearer " (which is 7 characters long).
        jwt = authHeader.substring(7);
        log.trace("Extracted JWT: {}", jwt);

        // 3. Extract the username (subject) from the JWT using the JwtService.
        // The username (often an email) is the principal identifier within the token.
        userEmail = jwtService.extractUsername(jwt);
        log.debug("Extracted username '{}' from JWT", userEmail);

        // 4. Validate authentication conditions:
        //    - userEmail must not be null (meaning a subject was successfully extracted from the JWT).
        //    - SecurityContextHolder.getContext().getAuthentication() == null:
        //      This ensures that the user is not already authenticated in the current security context.
        //      We only proceed with JWT validation if the user is not yet authenticated for this request.
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.trace("User '{}' is not authenticated, proceeding with token validation.", userEmail);

            // Load user details from the UserDetailsService based on the extracted userEmail.
            // This retrieves user information (e.g., roles, enabled status) from your data store.
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            log.trace("Loaded UserDetails for '{}'", userEmail);

            // 5. Validate the JWT token against the loaded UserDetails.
            // The JwtService checks for token validity (signature, expiration) and if it matches the user.
            if (jwtService.isTokenValid(jwt, userDetails)) {
                log.info("JWT token is valid for user '{}'. Authenticating.", userEmail);

                // If the token is valid, create an authentication token for Spring Security.
                // UsernamePasswordAuthenticationToken represents a successfully authenticated user.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, // The authenticated principal (UserDetails object).
                        null, // Credentials (password) are not needed after token validation.
                        userDetails.getAuthorities() // The user's granted authorities (roles).
                );

                // Attach additional details from the HTTP request to the authentication token.
                // This can include the remote IP address, session ID, etc., for auditing or further security checks.
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Set the authenticated UsernamePasswordAuthenticationToken in the SecurityContextHolder.
                // This is the crucial step that tells Spring Security the user is now authenticated
                // for the duration of this request, allowing subsequent security checks to pass.
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("User '{}' successfully authenticated and security context updated.", userEmail);
            } else {
                // Log a warning if the token is found to be invalid (e.g., expired, tampered).
                log.warn("JWT token is invalid for user '{}'", userEmail);
            }
        } else {
            // Log if authentication is skipped because the user is already authenticated or userEmail is null.
            log.trace("Skipping authentication for user '{}': already authenticated or userEmail is null.", userEmail);
        }

        // 6. Continue the filter chain.
        // This ensures the request proceeds to the next filter in the chain or to the target controller.
        // Without this, the request would be blocked after this filter.
        filterChain.doFilter(request, response);
    }
}
