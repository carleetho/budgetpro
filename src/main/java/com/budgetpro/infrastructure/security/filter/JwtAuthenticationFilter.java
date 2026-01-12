package com.budgetpro.infrastructure.security.filter;

import com.budgetpro.infrastructure.security.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro JWT para autenticación basada en tokens.
 * 
 * Implementa FIX-02: JWT Filter
 * - Extrae token JWT del header Authorization
 * - Valida token (por ahora básico, se puede extender)
 * - Establece SecurityContext con usuario autenticado
 * - Rechaza requests sin token válido (401)
 * 
 * Usa JwtTokenService para validar firma, expiración y extraer userId del token.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            // Extraer token del header Authorization
            String token = extractTokenFromRequest(request);
            
            if (token != null) {
                // Validar y procesar token
                Authentication authentication = validateAndCreateAuthentication(token, request);
                
                if (authentication != null) {
                    // Establecer autenticación en SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // Agregar userId al MDC para logging
                    if (authentication.getPrincipal() instanceof String userId) {
                        MDC.put("userId", userId);
                    }
                    
                    log.debug("Usuario autenticado: {}", authentication.getName());
                } else {
                    // Token inválido
                    log.warn("Token JWT inválido o expirado");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Token JWT inválido o expirado\"}");
                    return;
                }
            } else {
                // No hay token en el request
                // Spring Security rechazará el request si el endpoint requiere autenticación
                log.debug("Request sin token JWT - será rechazado si requiere autenticación");
            }
            
            // Continuar con la cadena de filtros
            filterChain.doFilter(request, response);
            
        } finally {
            // Limpiar SecurityContext y MDC al finalizar
            SecurityContextHolder.clearContext();
            MDC.remove("userId");
        }
    }

    /**
     * Extrae el token JWT del header Authorization.
     * 
     * @param request La request HTTP
     * @return El token JWT o null si no está presente
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Valida el token JWT y crea una Authentication.
     * 
     * Usa JwtTokenService para validar firma, expiración y extraer userId.
     * 
     * @param token El token JWT
     * @param request La request HTTP
     * @return Authentication o null si el token es inválido
     */
    private Authentication validateAndCreateAuthentication(String token, HttpServletRequest request) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        
        // Validar token y extraer userId usando servicio JWT
        String userId = jwtTokenService.validateAndExtractUserId(token);
        
        if (userId == null) {
            // Token inválido, expirado o sin userId
            return null;
        }
        
        // Crear Authentication con usuario autenticado
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userId, // principal (userId validado)
            null,   // credentials (no necesario con JWT)
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) // authorities
        );
        
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        return authentication;
    }
}
