package com.budgetpro.infrastructure.security.config;

import com.budgetpro.infrastructure.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de Spring Security para BudgetPro.
 * 
 * Implementa FIX-02: Spring Security Base
 * - SecurityFilterChain configurado
 * - JWT Filter integrado
 * - Rechaza requests anónimos en /api/**
 * - Permite /actuator/health para health checks
 * 
 * Según Directiva Maestra v2.0: "Ningún UseCase se ejecuta sin un SecurityContext validado"
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF para APIs REST (se maneja con JWT)
            .csrf(csrf -> csrf.disable())
            
            // Configurar autorización de endpoints
            .authorizeHttpRequests(auth -> auth
                // Permitir health checks sin autenticación (necesario para monitoreo)
                .requestMatchers("/actuator/health").permitAll()
                
                // Todos los endpoints /api/** requieren autenticación
                .requestMatchers("/api/**").authenticated()
                
                // Cualquier otro endpoint requiere autenticación
                .anyRequest().authenticated()
            )
            
            // Configurar sesiones como stateless (JWT no requiere sesión)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Agregar JWT Filter antes del filtro de autenticación por usuario/password
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
