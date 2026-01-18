package com.budgetpro.infrastructure.security.jwt;

import com.budgetpro.infrastructure.persistence.entity.seguridad.UsuarioEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio de JWT.
 */
@Service
public class JwtService {

    private final String secret;
    private final Duration expiration;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-hours:24}") long expirationHours) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET es obligatorio.");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres.");
        }
        this.secret = secret;
        this.expiration = Duration.ofHours(expirationHours);
    }

    public String generarToken(UsuarioEntity usuario) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(expiration)))
                .addClaims(Map.of(
                        "uid", usuario.getId().toString(),
                        "rol", usuario.getRol().name()
                ))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    public boolean esTokenValido(String token, String username) {
        String subject = extraerUsername(token);
        return subject.equals(username) && !estaExpirado(token);
    }

    public UUID extraerUsuarioId(String token) {
        String uid = extraerClaims(token).get("uid", String.class);
        return uid != null ? UUID.fromString(uid) : null;
    }

    private boolean estaExpirado(String token) {
        Date exp = extraerClaims(token).getExpiration();
        return exp.before(new Date());
    }

    private Claims extraerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException ex) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
