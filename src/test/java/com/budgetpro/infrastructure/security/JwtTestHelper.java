package com.budgetpro.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Helper para generar tokens JWT válidos en tests.
 * 
 * Usa el mismo secret key configurado en application-test.yml para generar
 * tokens que serán aceptados por JwtTokenService.
 */
public class JwtTestHelper {

    private static final String TEST_SECRET_KEY = "test-secret-key-minimum-32-characters-long-for-hs256-algorithm";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    /**
     * Genera un token JWT válido para un userId específico.
     * 
     * @param userId El ID del usuario (debe ser un UUID válido)
     * @return Token JWT firmado y válido
     */
    public static String generateValidToken(UUID userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3600000); // 1 hora de validez

        return Jwts.builder()
                .subject(userId.toString()) // "sub" claim
                .claim("userId", userId.toString()) // "userId" claim
                .issuedAt(now)
                .expiration(expiration)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Genera un token JWT válido para un userId específico con expiración personalizada.
     * 
     * @param userId El ID del usuario (debe ser un UUID válido)
     * @param expirationMinutes Minutos hasta la expiración
     * @return Token JWT firmado y válido
     */
    public static String generateValidToken(UUID userId, long expirationMinutes) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMinutes * 60 * 1000);

        return Jwts.builder()
                .subject(userId.toString()) // "sub" claim
                .claim("userId", userId.toString()) // "userId" claim
                .issuedAt(now)
                .expiration(expiration)
                .signWith(SECRET_KEY)
                .compact();
    }
}
