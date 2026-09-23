package com.vaultcore.vbms.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expiration;
    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {
        if (secret.length() < 32) throw new IllegalArgumentException("JWT_SECRET must be at least 32 characters");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expiration = expiration;
    }
    public String create(Integer userId, String email, String role) { return Jwts.builder().subject(email).claim("userId", userId).claim("role", role).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+expiration)).signWith(key).compact(); }
    public String email(String token) { return parse(token).getPayload().getSubject(); }
    public boolean valid(String token) { try { parse(token); return true; } catch (JwtException | IllegalArgumentException e) { return false; } }
    private Jws<Claims> parse(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token); }
}
