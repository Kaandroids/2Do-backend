package com.example._Do.auth.service;

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

/**
 * Service for handling JWT (JSON Web Token) operations.
 * <p>
 * This class provides methods to generate, validate, and extract information from JWT tokens.
 * Configuration values (secret key, expiration) are injected from the application.yml file.
 * </p>
 */
@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token The JWT token.
     * @return The username stored in the token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic method to extract specific claims using a resolver function.
     *
     * @param token          The JWT token.
     * @param claimsResolver Function to resolve the claim.
     * @param <T>            The type of the claim.
     * @return The extracted claim value.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a token for the user without extra claims.
     *
     * @param userDetails The user details.
     * @return Signed JWT string.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT token with custom claims, subject, issued time, and expiration.
     *
     * @param extraClaims Additional data to put into the token.
     * @param userDetails The user details.
     * @return Signed JWT string.
     */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Helper method to build the JWT token.
     *
     * @param extraClaims Extra claims to include.
     * @param userDetails User details (subject).
     * @param expiration  Expiration duration in milliseconds.
     * @return Built JWT token.
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the token by checking the username and expiration date.
     *
     * @param token       The JWT token.
     * @param userDetails The user details from the database.
     * @return True if token belongs to the user and is not expired, False otherwise.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // CRITICAL CHECK: Token username matches AND token is not expired
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if the token has expired.
     *
     * @param token The JWT token.
     * @return True if expired, False otherwise.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}