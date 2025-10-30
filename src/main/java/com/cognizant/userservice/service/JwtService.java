package com.cognizant.userservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    // Secret Key
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // How long JWT token will be valid
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // Extracts Username from JWT token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extracting specific Claims
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Generates token with no extra claims
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // Generates Token with extra Claims
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        // Build the token with claims, subject (username), issue date, expiration date, and sign it.
        return Jwts
                .builder()
                .setClaims(extraClaims) // Any extra info you want to put in the token
                .setSubject(userDetails.getUsername()) // The person the token is about
                .setIssuedAt(new Date(System.currentTimeMillis())) // When the token was created
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // When the token expires
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Sign the token with our secret key
                .compact(); // Build the final token string
    }

    // Validating the Token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Checking if the token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extracting the expiration
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Core method to reading Token's Claims
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey()) // Use our secret key to verify the token's signature
                .build()
                .parseClaimsJws(token) // Parse the token
                .getBody(); // Get the claims (the data inside the token)
    }

    /**
     * Converts a base64-encoded secret key into a Key object.
     * This key is used for securely signing and verifying JWTs.
     *
     * @return The signing key used for JWT encryption.
     */
    private Key getSignInKey() {
        // Decode the base64-encoded secret key into a byte array
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        // Generate an HMAC-SHA key from the decoded bytes
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
