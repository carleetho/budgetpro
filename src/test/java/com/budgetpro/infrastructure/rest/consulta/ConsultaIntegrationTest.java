package com.budgetpro.infrastructure.rest.consulta;

import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository;
import com.budgetpro.infrastructure.security.JwtTestHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para los endpoints de consulta (Query Side).
 * 
 * Verifica que los endpoints GET funcionen correctamente:
 * - GET /api/v1/proyectos
 * - GET /api/v1/proyectos/{proyectoId}/presupuestos
 * - GET /api/v1/recursos?search=...
 */
class ConsultaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProyectoJpaRepository proyectoJpaRepository;

    @Autowired
    private PresupuestoJpaRepository presupuestoJpaRepository;

    @Autowired
    private RecursoJpaRepository recursoJpaRepository;

    @LocalServerPort
    private int port;

    private String baseUrl;
    private UUID proyectoId1;
    private UUID proyectoId2;
    private UUID presupuestoId1;
    private UUID recursoId1;
    private UUID recursoId2;
    private String jwtToken;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
        
        // Configurar usuario de prueba para JWT
        testUserId = UUID.randomUUID();
        jwtToken = JwtTestHelper.generateValidToken(testUserId);
        
        // Limpiar base de datos
        presupuestoJpaRepository.deleteAll();
        proyectoJpaRepository.deleteAll();
        recursoJpaRepository.deleteAll();

        // Crear proyectos de prueba usando reflexión para acceder al constructor protected
        proyectoId1 = UUID.randomUUID();
        proyectoId2 = UUID.randomUUID();
        
        ProyectoEntity proyecto1 = proyectoJpaRepository.save(crearProyecto(proyectoId1, "Proyecto Test 1", "EN_EJECUCION"));
        ProyectoEntity proyecto2 = proyectoJpaRepository.save(crearProyecto(proyectoId2, "Proyecto Test 2", "CERRADO"));

        // Crear presupuesto para proyecto1
        presupuestoId1 = UUID.randomUUID();
        PresupuestoEntity presupuesto1 = new PresupuestoEntity(
                presupuestoId1,
                proyectoId1,
                false,
                null
        );
        presupuestoJpaRepository.save(presupuesto1);

        // Crear recursos de prueba
        recursoId1 = UUID.randomUUID();
        RecursoEntity recurso1 = new RecursoEntity();
        recurso1.setId(recursoId1);
        recurso1.setNombre("CEMENTO GRIS");
        recurso1.setNombreNormalizado("CEMENTO GRIS");
        recurso1.setTipo(com.budgetpro.domain.recurso.model.TipoRecurso.MATERIAL);
        recurso1.setUnidadBase("KG");
        recurso1.setEstado(com.budgetpro.domain.recurso.model.EstadoRecurso.ACTIVO);
        recurso1.setCreatedBy(UUID.randomUUID());
        recursoJpaRepository.save(recurso1);

        recursoId2 = UUID.randomUUID();
        RecursoEntity recurso2 = new RecursoEntity();
        recurso2.setId(recursoId2);
        recurso2.setNombre("ARENA FINA");
        recurso2.setNombreNormalizado("ARENA FINA");
        recurso2.setTipo(com.budgetpro.domain.recurso.model.TipoRecurso.MATERIAL);
        recurso2.setUnidadBase("M3");
        recurso2.setEstado(com.budgetpro.domain.recurso.model.EstadoRecurso.ACTIVO);
        recurso2.setCreatedBy(UUID.randomUUID());
        recursoJpaRepository.save(recurso2);
    }

    @Test
    void testListarProyectos_Todos() {
        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/proyectos",
                HttpMethod.GET,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<com.budgetpro.application.proyecto.dto.ProyectoResponse> proyectos = 
                    mapper.readValue(response.getBody(), new TypeReference<List<com.budgetpro.application.proyecto.dto.ProyectoResponse>>() {});
            
            assertThat(proyectos).hasSize(2);
            assertThat(proyectos).extracting("id").contains(proyectoId1, proyectoId2);
        } catch (Exception e) {
            throw new AssertionError("Error al parsear respuesta: " + response.getBody(), e);
        }
    }

    @Test
    void testListarProyectos_PorEstado() {
        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/proyectos?estado=EN_EJECUCION",
                HttpMethod.GET,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<com.budgetpro.application.proyecto.dto.ProyectoResponse> proyectos = 
                    mapper.readValue(response.getBody(), new TypeReference<List<com.budgetpro.application.proyecto.dto.ProyectoResponse>>() {});
            
            assertThat(proyectos).hasSize(1);
            assertThat(proyectos.get(0).id()).isEqualTo(proyectoId1);
            assertThat(proyectos.get(0).estado()).isEqualTo("EN_EJECUCION");
        } catch (Exception e) {
            throw new AssertionError("Error al parsear respuesta: " + response.getBody(), e);
        }
    }

    @Test
    void testListarPresupuestos_PorProyecto() {
        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/proyectos/" + proyectoId1 + "/presupuestos",
                HttpMethod.GET,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<com.budgetpro.application.presupuesto.dto.PresupuestoResponse> presupuestos = 
                    mapper.readValue(response.getBody(), new TypeReference<List<com.budgetpro.application.presupuesto.dto.PresupuestoResponse>>() {});
            
            assertThat(presupuestos).hasSize(1);
            assertThat(presupuestos.get(0).id()).isEqualTo(presupuestoId1);
            assertThat(presupuestos.get(0).proyectoId()).isEqualTo(proyectoId1);
        } catch (Exception e) {
            throw new AssertionError("Error al parsear respuesta: " + response.getBody(), e);
        }
    }

    @Test
    void testBuscarRecursos_PorNombre() {
        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/recursos?search=cemento",
                HttpMethod.GET,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<com.budgetpro.application.recurso.dto.RecursoSearchResponse> recursos = 
                    mapper.readValue(response.getBody(), new TypeReference<List<com.budgetpro.application.recurso.dto.RecursoSearchResponse>>() {});
            
            assertThat(recursos).hasSize(1);
            assertThat(recursos.get(0).id()).isEqualTo(recursoId1);
            assertThat(recursos.get(0).nombre()).isEqualTo("CEMENTO GRIS");
        } catch (Exception e) {
            throw new AssertionError("Error al parsear respuesta: " + response.getBody(), e);
        }
    }

    @Test
    void testBuscarRecursos_SinFiltro() {
        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/recursos",
                HttpMethod.GET,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<com.budgetpro.application.recurso.dto.RecursoSearchResponse> recursos = 
                    mapper.readValue(response.getBody(), new TypeReference<List<com.budgetpro.application.recurso.dto.RecursoSearchResponse>>() {});
            
            assertThat(recursos).hasSize(2);
            assertThat(recursos).extracting("id").contains(recursoId1, recursoId2);
        } catch (Exception e) {
            throw new AssertionError("Error al parsear respuesta: " + response.getBody(), e);
        }
    }

    @Test
    void testBuscarRecursos_ConLimite() {
        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/recursos?limit=1",
                HttpMethod.GET,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<com.budgetpro.application.recurso.dto.RecursoSearchResponse> recursos = 
                    mapper.readValue(response.getBody(), new TypeReference<List<com.budgetpro.application.recurso.dto.RecursoSearchResponse>>() {});
            
            assertThat(recursos).hasSize(1);
        } catch (Exception e) {
            throw new AssertionError("Error al parsear respuesta: " + response.getBody(), e);
        }
    }

    /**
     * Helper method para crear ProyectoEntity usando reflexión (constructor es protected).
     */
    private ProyectoEntity crearProyecto(UUID id, String nombre, String estado) {
        try {
            Constructor<ProyectoEntity> constructor = ProyectoEntity.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            ProyectoEntity proyecto = constructor.newInstance();
            proyecto.setId(id);
            proyecto.setNombre(nombre);
            proyecto.setEstado(estado);
            proyecto.setCreatedAt(LocalDateTime.now());
            proyecto.setUpdatedAt(LocalDateTime.now());
            return proyecto;
        } catch (Exception e) {
            throw new RuntimeException("Error al crear ProyectoEntity", e);
        }
    }
}
