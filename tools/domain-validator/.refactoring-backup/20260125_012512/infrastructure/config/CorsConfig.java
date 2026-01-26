package com.budgetpro.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing) para permitir
 * que el frontend Next.js se comunique con el backend Spring Boot.
 * 
 * Esta configuración permite solicitudes desde http://localhost:3000
 * y otros orígenes durante el desarrollo.
 */
@Configuration
public class CorsConfig {

    /**
     * Configuración de CORS para todas las rutas del API.
     * 
     * @return WebMvcConfigurer con la configuración de CORS
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // Orígenes permitidos: frontend Next.js y cualquier origen localhost
                        // Usamos allowedOriginPatterns para permitir múltiples orígenes con credenciales
                        .allowedOriginPatterns("http://localhost:*", "*")
                        // Métodos HTTP permitidos
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // Headers permitidos (todos)
                        .allowedHeaders("*")
                        // Permitir credenciales (cookies, auth headers)
                        .allowCredentials(true)
                        // Tiempo de cache para preflight requests (1 hora)
                        .maxAge(3600);
            }
        };
    }
}
