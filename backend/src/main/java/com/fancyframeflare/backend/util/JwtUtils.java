package com.fancyframeflare.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {
    // 至少32位密钥
    private final String secret = "HealthyBaySecretKey12345678901234567890";
    private final SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    private final long expirationMs = 86400000; // 24h

    public String generateToken(Long userId, String username, Long classId, Long roomId) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("classId", classId)
                .claim("roomId", roomId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    // 重载方法支持直接放入 claims
    public String generateToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
