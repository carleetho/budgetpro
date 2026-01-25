package com.budgetpro.infrastructure.rest.rrhh;

import com.budgetpro.application.rrhh.dto.EmpleadoResponse;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;
import com.budgetpro.domain.rrhh.model.TipoEmpleado;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.repository.rrhh.EmpleadoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.rrhh.HistorialLaboralJpaRepository;
import com.budgetpro.infrastructure.rest.rrhh.dto.ActualizarEmpleadoRequest;
import com.budgetpro.infrastructure.rest.rrhh.dto.CrearEmpleadoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmpleadoControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmpleadoJpaRepository empleadoJpaRepository;

    @Autowired
    private HistorialLaboralJpaRepository historialLaboralJpaRepository;

    @LocalServerPort
    private int port;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/rrhh/empleados";
        historialLaboralJpaRepository.deleteAll();
        empleadoJpaRepository.deleteAll();
    }

    @Test
    void testCrearEmpleado_HappyPath() {
        CrearEmpleadoRequest request = new CrearEmpleadoRequest("Juan", "Perez", "12345678", "juan.perez@example.com",
                "555-1234", "Calle 123", LocalDate.now().minusYears(1), new BigDecimal("2500.00"), "Ingeniero Junior",
                TipoEmpleado.PERMANENTE);

        ResponseEntity<EmpleadoResponse> response = restTemplate.postForEntity(baseUrl, request,
                EmpleadoResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().nombre()).isEqualTo("Juan");
        assertThat(response.getBody().numeroIdentificacion()).isEqualTo("12345678");
        assertThat(response.getHeaders().getLocation()).isNotNull();
    }

    @Test
    void testCrearEmpleado_Duplicado() {
        testCrearEmpleado_HappyPath();

        CrearEmpleadoRequest request = new CrearEmpleadoRequest("Otro", "Nombre", "12345678", // Mismo
                                                                                              // numeroIdentificacion
                "otro.email@example.com", null, null, LocalDate.now(), new BigDecimal("1000.00"), "Puesto",
                TipoEmpleado.TEMPORAL);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void testObtenerPorId() {
        CrearEmpleadoRequest request = new CrearEmpleadoRequest("Maria", "Lopez", "87654321", "maria@example.com", null,
                null, LocalDate.now(), new BigDecimal("3000.00"), "Gerente", TipoEmpleado.PERMANENTE);
        ResponseEntity<EmpleadoResponse> createResponse = restTemplate.postForEntity(baseUrl, request,
                EmpleadoResponse.class);
        String id = createResponse.getBody().id();

        ResponseEntity<EmpleadoResponse> getResponse = restTemplate.getForEntity(baseUrl + "/" + id,
                EmpleadoResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().numeroIdentificacion()).isEqualTo("87654321");
    }

    @Test
    void testListarEmpleados() {
        testCrearEmpleado_HappyPath();

        ResponseEntity<EmpleadoResponse[]> response = restTemplate.getForEntity(baseUrl, EmpleadoResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void testActualizarSalarioYHistorial() {
        CrearEmpleadoRequest createRequest = new CrearEmpleadoRequest("Update", "Test", "99999999",
                "update@example.com", null, null, LocalDate.now().minusMonths(1), new BigDecimal("1000.00"), "Puesto A",
                TipoEmpleado.PERMANENTE);
        ResponseEntity<EmpleadoResponse> createResponse = restTemplate.postForEntity(baseUrl, createRequest,
                EmpleadoResponse.class);
        String id = createResponse.getBody().id();

        ActualizarEmpleadoRequest updateRequest = new ActualizarEmpleadoRequest(null, null, null, null, null,
                new BigDecimal("1500.00"), "Puesto B", LocalDate.now());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ActualizarEmpleadoRequest> entity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<EmpleadoResponse> updateResponse = restTemplate.exchange(baseUrl + "/" + id, HttpMethod.PUT,
                entity, EmpleadoResponse.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().salarioActual()).isEqualByComparingTo("1500.00");
        assertThat(updateResponse.getBody().puestoActual()).isEqualTo("Puesto B");

        // Verify HistorialLaboral count (1 initial + 1 update)
        long historyCount = historialLaboralJpaRepository.count();
        assertThat(historyCount).isEqualTo(2);
    }

    @Test
    void testInactivarEmpleado() {
        CrearEmpleadoRequest createRequest = new CrearEmpleadoRequest("Delete", "Me", "00000000", "delete@example.com",
                null, null, LocalDate.now(), new BigDecimal("1000.00"), "Puesto", TipoEmpleado.PERMANENTE);
        ResponseEntity<EmpleadoResponse> createResponse = restTemplate.postForEntity(baseUrl, createRequest,
                EmpleadoResponse.class);
        String id = createResponse.getBody().id();

        restTemplate.delete(baseUrl + "/" + id);

        ResponseEntity<EmpleadoResponse> getResponse = restTemplate.getForEntity(baseUrl + "/" + id,
                EmpleadoResponse.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().estado()).isEqualTo("INACTIVO");
    }
}
