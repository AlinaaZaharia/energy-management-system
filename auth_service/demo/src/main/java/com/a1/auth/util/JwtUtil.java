package com.a1.auth.util;

import com.a1.auth.entity.Credential;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            logger.error(" JWT secret is too short! Must be at least 256 bits (32 characters)");
            throw new IllegalArgumentException("JWT secret must be at least 256 bits (32 characters)");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Credential credential) {
        logger.debug("Generating token for user: {}", credential.getUsername());

        try {
            Map<String, Object> claims = new HashMap<>();

            UUID userIdToUse = credential.getUserId() != null ? credential.getUserId() : credential.getId();
            logger.debug("Using userId: {}", userIdToUse);

            claims.put("userId", userIdToUse.toString());
            claims.put("username", credential.getUsername());
            claims.put("role", credential.getRole().name());

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(credential.getUsername())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();

            logger.debug("Token generated successfully (first 20 chars): {}", token.substring(0, 20));
            return token;

        } catch (Exception e) {
            logger.error("Error generating token", e);
            throw new RuntimeException("Error generating token", e);
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        String userIdStr = (String) extractAllClaims(token).get("userId");
        return UUID.fromString(userIdStr);
    }

    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            logger.error("Token validation failed", e);
            return false;
        }
    }
}