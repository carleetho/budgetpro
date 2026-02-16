package com.budgetpro.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación de API.
 * 
 * La documentación interactiva estará disponible en:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * - OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BudgetPro API")
                        .version("1.0.0")
                        .description("""
                                API REST para gestión de presupuestos y compras de proyectos de construcción.
                                
                                ## Autenticación
                                Todas las operaciones requieren autenticación mediante Bearer Token (JWT).
                                Obtén el token mediante el endpoint de autenticación y úsalo en el header:
                                ```
                                Authorization: Bearer <token>
                                ```
                                
                                ## Módulos Principales
                                - **Compras**: Gestión de órdenes de compra y proveedores
                                - **Presupuestos**: Gestión de presupuestos y partidas
                                - **Inventario**: Control de stock y movimientos
                                - **Proyectos**: Gestión de proyectos y obras
                                """)
                        .contact(new Contact()
                                .name("BudgetPro Team")
                                .email("support@budgetpro.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://budgetpro.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo"),
                        new Server()
                                .url("https://api.budgetpro.com")
                                .description("Servidor de producción")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        Autenticación mediante JWT (JSON Web Token).
                                        
                                        1. Obtén el token mediante POST /api/v1/auth/login
                                        2. Incluye el token en el header: Authorization: Bearer <token>
                                        3. El token expira después del tiempo configurado (por defecto 24 horas)
                                        """)))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
