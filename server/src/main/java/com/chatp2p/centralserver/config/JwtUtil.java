package com.chatp2p.centralserver.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;

@Component
public class JwtUtil {

    @Value("${SERVER_JWT_SECRET}")
    private String secret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId) {
        return Jwts.builder().subject(userId.toString()).signWith(getSigningKey()).compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();

        return Long.parseLong(claims.getSubject());
    }
}