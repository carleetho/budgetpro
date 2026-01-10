package com.budgetpro.infrastructure.rest.recurso;

import com.budgetpro.application.recurso.dto.RecursoResponse;
import com.budgetpro.domain.recurso.model.TipoRecurso;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository;
import com.budgetpro.infrastructure.rest.recurso.dto.CrearRecursoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de Integración para el controlador REST de Recursos.
 * 
 * Prueba el flujo completo "Alta de Recurso" desde HTTP hasta persistencia,
 * incluyendo normalización, validación y manejo de errores.
 * 
 * Extiende de AbstractIntegrationTest que proporciona:
 * - Base de datos PostgreSQL real en contenedor
 * - Migraciones de Flyway ejecutadas automáticamente
 * - Contexto Spring Boot completo
 */
class RecursoControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RecursoJpaRepository recursoJpaRepository;

    @LocalServerPort
    private int port;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/recursos";
        // Limpiar la base de datos antes de cada test
        recursoJpaRepository.deleteAll();
    }

    @Test
    void testCrearRecurso_HappyPath() {
        // Given: JSON válido con nombre sin normalizar
        CrearRecursoRequest request = new CrearRecursoRequest(
            "  cemento   gris  ",  // Nombre sin normalizar (será normalizado a "CEMENTO GRIS")
            "MATERIAL",
            "KG",
            null,
            false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CrearRecursoRequest> httpEntity = new HttpEntity<>(request, headers);

        // When: Enviar request POST
        ResponseEntity<RecursoResponse> response = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            httpEntity,
            RecursoResponse.class
        );

        // Then: Assert Status 201
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Assert: El ID en la respuesta no es nulo
        RecursoResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.id()).isNotNull();

        // Assert: El nombre viene NORMALIZADO (Mayúsculas/Trim)
        assertThat(body.nombre()).isEqualTo("CEMENTO GRIS");

        // Assert: Header 'Location' presente
        assertThat(response.getHeaders().getLocation()).isNotNull();

        // Assert: Consultar el repositorio RecursoJpaRepository y verificar que se guardó en BD
        Optional<RecursoEntity> entityOpt = recursoJpaRepository.findById(body.id());
        assertThat(entityOpt).isPresent();

        RecursoEntity entity = entityOpt.get();
        assertThat(entity.getNombreNormalizado()).isEqualTo("CEMENTO GRIS");
        assertThat(entity.getTipo()).isEqualTo(TipoRecurso.MATERIAL);
        assertThat(entity.getUnidadBase()).isEqualTo("KG");
    }

    @Test
    void testCrearRecurso_Duplicado() {
        // Given: Crear un recurso previamente
        UUID idExistente = UUID.randomUUID();
        RecursoEntity recursoExistente = new RecursoEntity();
        recursoExistente.setId(idExistente);
        recursoExistente.setNombre("CEMENTO GRIS");
        recursoExistente.setNombreNormalizado("CEMENTO GRIS");
        recursoExistente.setTipo(TipoRecurso.MATERIAL);
        recursoExistente.setUnidadBase("KG");
        recursoExistente.setCreatedBy(UUID.randomUUID());
        recursoJpaRepository.save(recursoExistente);

        // When: Intentar crear otro con el mismo nombre (variando mayúsculas)
        CrearRecursoRequest requestDuplicado = new CrearRecursoRequest(
            "cemento GRIS",  // Variación: diferente mayúscula/minúscula
            "MATERIAL",
            "TON",
            null,
            false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CrearRecursoRequest> httpEntity = new HttpEntity<>(requestDuplicado, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            httpEntity,
            String.class
        );

        // Then: Assert Status 409 CONFLICT
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        
        // Verificar que NO se creó un segundo recurso
        long count = recursoJpaRepository.count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testCrearRecurso_Invalido() {
        // Given: Enviar nombre vacío
        CrearRecursoRequest requestInvalido = new CrearRecursoRequest(
            "",  // Nombre vacío
            "MATERIAL",
            "KG",
            null,
            false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CrearRecursoRequest> httpEntity = new HttpEntity<>(requestInvalido, headers);

        // When: Enviar request POST
        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            httpEntity,
            String.class
        );

        // Then: Assert Status 400 BAD REQUEST
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Verificar que NO se guardó en la BD
        long count = recursoJpaRepository.count();
        assertThat(count).isEqualTo(0);
    }
}
