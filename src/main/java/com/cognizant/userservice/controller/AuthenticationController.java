package com.cognizant.userservice.controller;

import com.cognizant.userservice.dto.AuthenticationRequest;
import com.cognizant.userservice.dto.AuthenticationResponse;
import com.cognizant.userservice.service.JwtService;
import com.cognizant.userservice.service.MyUserDetailsService; // Use MyUserDetailsService
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor // Lombok annotation to generate constructor for final fields with Non-Null values
@Slf4j
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final MyUserDetailsService userDetailsService; // Use MyUserDetailsService
    private final JwtService jwtService;


    // Handles user authentication (login) and generates a JWT.
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        log.info("Authentication request for user: {}", request.getUsername());
        // 1. Authenticate the user using Spring Security's AuthenticationManager.
        // This will trigger the DaoAuthenticationProvider (configured in SecurityConfig)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. If authentication succeeds, load the user details again (or reuse from authentication object).
        // This is done to ensure we have the full UserDetails object to generate the token.
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        // 3. Generate the JWT using our JwtService.
        String jwtToken = jwtService.generateToken(userDetails);

        // 4. Return the JWT in the response.
        log.info("Authentication successful for user: {}", request.getUsername());
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .token(jwtToken)
                .build());
    }
}
