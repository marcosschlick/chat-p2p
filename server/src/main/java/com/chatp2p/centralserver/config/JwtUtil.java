package com.chatp2p.centralserver.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {
    private static final String SECRET = "MySuperSecretKeyThatIsLongEnoughForHS256";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .signWith(key)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }
}