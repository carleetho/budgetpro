package com.budgetpro.infrastructure.security.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servicio para acceder al SecurityContext de forma segura.
 * 
 * Proporciona métodos utilitarios para obtener información del usuario autenticado
 * desde el SecurityContext establecido por JwtAuthenticationFilter.
 * 
 * Según Directiva Maestra v2.0: "Ningún UseCase se ejecuta sin un SecurityContext validado"
 */
@Service
public class SecurityContextService {

    /**
     * Obtiene el ID del usuario actual desde el SecurityContext.
     * 
     * @return El UUID del usuario actual
     * @throws IllegalStateException si no hay usuario autenticado
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado en el SecurityContext");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof String userId) {
            try {
                return UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("El userId del SecurityContext no es un UUID válido: " + userId, e);
            }
        }
        
        throw new IllegalStateException("El principal del SecurityContext no es un String (userId): " + principal);
    }

    /**
     * Verifica si hay un usuario autenticado en el SecurityContext.
     * 
     * @return true si hay usuario autenticado, false en caso contrario
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
