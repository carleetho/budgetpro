package com.budgetpro.infrastructure;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Clase base abstracta para tests de integración.
 * 
 * Configura un contenedor PostgreSQL usando Testcontainers y asegura que:
 * - Flyway ejecute las migraciones automáticamente al iniciar el contexto
 * - Cada test tenga una base de datos PostgreSQL limpia y real
 * - La configuración esté aislada del entorno de producción
 * 
 * Para usar esta clase, simplemente hereda de ella:
 * 
 * <pre>
 * class MiTest extends AbstractIntegrationTest {
 *     // Tu test aquí
 * }
 * </pre>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    /**
     * Contenedor PostgreSQL estático compartido entre todos los tests de la misma clase.
     * Se inicia una sola vez y se reutiliza para todos los métodos de test.
     * 
     * La anotación @ServiceConnection conecta automáticamente este contenedor
     * con Spring Boot DataSource, eliminando la necesidad de configuración manual.
     */
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("budgetpro_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true); // Permite reutilizar el contenedor entre ejecuciones

}
