package com.budgetpro.infrastructure.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Servicio para validar tokens JWT.
 * 
 * Implementa validación real de JWT con:
 * - Validación de firma
 * - Validación de expiración
 * - Extracción de claims (userId)
 * 
 * Según Directiva Maestra v2.0: JWT debe validar firma, expiración y revocación
 */
@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);
    private static final String USER_ID_CLAIM = "userId";
    private static final String SUB_CLAIM = "sub";

    private final SecretKey secretKey;

    public JwtTokenService(@Value("${jwt.secret-key:}") String secretKeyString) {
        if (secretKeyString == null || secretKeyString.isBlank()) {
            throw new IllegalStateException(
                "JWT_SECRET_KEY debe estar configurado. Configure la variable de entorno JWT_SECRET_KEY o la propiedad jwt.secret-key"
            );
        }
        
        // Generar SecretKey desde el string (mínimo 256 bits para HS256)
        if (secretKeyString.length() < 32) {
            throw new IllegalArgumentException(
                "JWT_SECRET_KEY debe tener al menos 32 caracteres para seguridad HS256"
            );
        }
        
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Valida un token JWT y extrae el userId.
     * 
     * @param token El token JWT a validar
     * @return El userId extraído del token, o null si el token es inválido
     */
    public String validateAndExtractUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            // Parsear y validar el token JWT
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Validar expiración
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                log.warn("Token JWT expirado: exp={}", expiration);
                return null;
            }

            // Extraer userId del claim "userId" o "sub"
            String userId = claims.get(USER_ID_CLAIM, String.class);
            if (userId == null) {
                userId = claims.getSubject(); // Fallback a "sub"
            }

            if (userId == null || userId.isBlank()) {
                log.warn("Token JWT no contiene userId ni sub claim");
                return null;
            }

            // Validar que userId sea un UUID válido
            try {
                UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                log.warn("Token JWT contiene userId inválido (no es UUID): {}", userId);
                return null;
            }

            return userId;

        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.warn("Token JWT con firma inválida: {}", e.getMessage());
            return null;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token JWT expirado: {}", e.getMessage());
            return null;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Token JWT malformado: {}", e.getMessage());
            return null;
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("Token JWT no soportado: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error inesperado al validar token JWT", e);
            return null;
        }
    }
}
