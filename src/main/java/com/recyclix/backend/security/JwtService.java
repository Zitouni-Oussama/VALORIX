package com.recyclix.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final SecurityProperties props;

    public JwtService(SecurityProperties props) {
        this.props = props;

        // Ton secret est en hex long. On peut l'utiliser comme string brute.
        // Si tu veux une vraie clé hex => je te donne version decode hex.
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(AccountPrincipal principal) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getTtlSeconds());

        return Jwts.builder()
                .issuer(props.getIssuer())
                .subject(principal.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(Map.of(
                        SecurityConstants.CLAIM_ACCOUNT_ID, principal.getId(),
                        SecurityConstants.CLAIM_ROLE, principal.getRole()
                ))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Jws<Claims> parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(props.getIssuer())
                .build()
                .parseSignedClaims(token);
    }

    public String extractEmail(String token) {
        return parseAndValidate(token).getPayload().getSubject();
    }

    public Long extractAccountId(String token) {
        Object v = parseAndValidate(token).getPayload().get(SecurityConstants.CLAIM_ACCOUNT_ID);
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Long l) return l;
        if (v instanceof String s) return Long.parseLong(s);
        return null;
    }

    public String extractRole(String token) {
        Object v = parseAndValidate(token).getPayload().get(SecurityConstants.CLAIM_ROLE);
        return v == null ? null : v.toString();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token) {
        Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
        return true;
    }
}