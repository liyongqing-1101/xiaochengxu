package com.wxjiaozi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.mini-expiration}")
    private long miniExpiration;

    @Value("${app.jwt.admin-expiration}")
    private long adminExpiration;

    /**
     * Generate a JWT token for the given user.
     * Uses adminExpiration if the role contains "ADMIN", otherwise miniExpiration.
     */
    public String generateToken(Long userId, String openid, String role) {
        long expiration = (role != null && role.toUpperCase().contains("ADMIN"))
                ? adminExpiration
                : miniExpiration;

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("openid", openid)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate a refresh token with 30-day expiration.
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 30L * 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate a JWT token. Returns true if the token is valid and not expired.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract the user ID from a JWT token.
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * Extract the openid from a JWT token.
     */
    public String getOpenidFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("openid", String.class);
    }

    /**
     * Extract the role from a JWT token.
     */
    public String getRoleFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Extract the expiration Date from a JWT token.
     * Returns null if the token is invalid or cannot be parsed.
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the signing key derived from the configured secret.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /**
     * Extract all claims from a JWT token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
