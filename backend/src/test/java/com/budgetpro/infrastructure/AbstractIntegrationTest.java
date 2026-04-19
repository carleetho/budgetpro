package com.budgetpro.infrastructure;

import com.budgetpro.domain.finanzas.presupuesto.model.SubpresupuestoNaming;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.entity.SubpresupuestoEntity;
import com.budgetpro.infrastructure.persistence.repository.SubpresupuestoJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected SubpresupuestoJpaRepository subpresupuestoJpaRepository;

    /** Subpresupuesto sintético "Principal" (trigger Flyway V41 tras insert presupuesto). */
    protected SubpresupuestoEntity principalSub(PresupuestoEntity presupuesto) {
        return subpresupuestoJpaRepository
                .findByPresupuesto_IdAndNombre(presupuesto.getId(), SubpresupuestoNaming.PRINCIPAL)
                .orElseThrow(() -> new IllegalStateException(
                        "Subpresupuesto Principal no existe para presupuesto " + presupuesto.getId()));
    }
    static {
        // NOTA: Docker 29.1.4 soporta API 1.52, pero Testcontainers 1.20.4 usa docker-java 3.4.0
        // que tiene API 1.32 embebida. Esto causa incompatibilidad.
        // 
        // SOLUCIONES (ver SOLUCION_DOCKER_TESTS.md):
        // 1. Configurar Docker daemon para aceptar API 1.32 (temporal)
        // 2. Actualizar Testcontainers cuando haya versión compatible
        // 3. Usar Docker Compose en lugar de Testcontainers
        //
        // Por ahora, intentamos configurar para usar la versión más compatible posible
        System.setProperty("docker.api.version", "1.44"); // Mínimo requerido por Docker 29.1.4
        System.setProperty("docker.client.strategy", "org.testcontainers.dockerclient.UnixSocketClientProviderStrategy");
        // Configurar socket de Docker
        if (System.getenv("DOCKER_HOST") == null) {
            System.setProperty("DOCKER_HOST", "unix:///var/run/docker.sock");
        }
        // Forzar actualización del cliente Docker en Testcontainers
        System.setProperty("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", "/var/run/docker.sock");
    }

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
