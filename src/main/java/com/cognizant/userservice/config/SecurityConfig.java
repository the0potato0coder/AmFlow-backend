package com.cognizant.userservice.config;

import com.cognizant.userservice.service.MyUserDetailsService; // Ensure this import is correct
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Import for filter
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // Lombok annotation to generate constructor for final fields
public class SecurityConfig {

    private final MyUserDetailsService userDetailsService; // Use MyUserDetailsService
    private final JwtAuthenticationFilter jwtAuthFilter; // Inject our custom JWT filter

    // No need to inject JwtService directly here, as it's used within JwtAuthenticationFilter
    @Bean
    public AuthenticationProvider authenticationProvider() { // Renamed authProvider to authenticationProvider for clarity
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF as JWTs handle this for stateless APIs
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Configure CORS
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow OPTIONS requests
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll() // Allow new user registration
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/authenticate").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/attendance/checkin").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/attendance/checkout").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/attendance/my-all").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.POST,"/api/v1/leaves/apply").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/leaves/my-leaves").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/leaves/pending").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/leaves/{leaveId}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/attendance/adjustments/request").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/attendance/adjustments/pending").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/attendance/adjustments/{adjustmentId}/approve").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/attendance/adjustments/{adjustmentId}/reject").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/attendance/user/{userId}/stats/weekly").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/attendance/user/{userId}/stats/monthly").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/attendance/user/{userId}/all").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/attendance/my-stats/weekly").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/attendance/my-stats/monthly").hasAnyAuthority("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/{id}").hasAuthority("ADMIN")
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated() // All other requests require authentication
                )
                // Making the Session Management Stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Add our custom JWT authentication filter before Spring Security's UsernamePasswordAuthenticationFilter
                .authenticationProvider(authenticationProvider()) // Set our custom authentication provider
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000",
                "https://amflow-frontend.netlify.app"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
