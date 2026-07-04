package com.leaveflow.backend.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

   private final SecretKey signingKey;
   private final long accessTokenExpirationMs;
   private final long refreshTokenExpirationMs;

   public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
                   @Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
       this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
       this.accessTokenExpirationMs = accessTokenExpirationMs;
       this.refreshTokenExpirationMs = refreshTokenExpirationMs;
   }

   public String generateAccessToken(Long userId, String email, String role) {
       return buildToken(userId, email, role, accessTokenExpirationMs, "access");
   }

   public String generateRefreshToken(Long userId, String email, String role) {
       return buildToken(userId, email, role, refreshTokenExpirationMs, "refresh");
   }

   private String buildToken(Long userId, String email, String role, long expirationMs, String tokenType) {
       Date now = new Date();
       Date expiry = new Date(now.getTime() + expirationMs);
       return Jwts.builder()
               .subject(email)
               .claim("userId", userId)
               .claim("role", role)
               .claim("type", tokenType)
               .issuedAt(now)
               .expiration(expiry)
               .signWith(signingKey)
               .compact();
   }

   public String extractEmail(String token) {
       return extractClaim(token, Claims::getSubject);
   }

   public Long extractUserId(String token) {
       return extractAllClaims(token).get("userId", Long.class);
   }

   public String extractRole(String token) {
       return extractAllClaims(token).get("role", String.class);
   }

   public boolean isTokenValid(String token, String expectedEmail) {
       String email = extractEmail(token);
       return email.equals(expectedEmail) && !isTokenExpired(token);
   }

   private boolean isTokenExpired(String token) {
       return extractClaim(token, Claims::getExpiration).before(new Date());
   }

   private <T> T extractClaim(String token, Function<Claims, T> resolver) {
       return resolver.apply(extractAllClaims(token));
   }

   private Claims extractAllClaims(String token) {
       return Jwts.parser()
               .verifyWith(signingKey)
               .build()
               .parseSignedClaims(token)
               .getPayload();
   }
}
