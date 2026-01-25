package com.budgetpro.infrastructure.rest.rrhh;

import com.budgetpro.application.rrhh.dto.CostosLaboralesResponse;
import com.budgetpro.domain.rrhh.model.TipoEmpleado;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.AsistenciaRegistroEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.EmpleadoEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.HistorialLaboralEntity;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.rrhh.AsistenciaRegistroJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.rrhh.EmpleadoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.rrhh.HistorialLaboralJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CostosLaboralesControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmpleadoJpaRepository empleadoJpaRepository;

    @Autowired
    private ProyectoJpaRepository proyectoJpaRepository;

    @Autowired
    private AsistenciaRegistroJpaRepository asistenciaRegistroJpaRepository;

    @Autowired
    private HistorialLaboralJpaRepository historialLaboralJpaRepository;

    @LocalServerPort
    private int port;

    private String baseUrl;
    private UUID proyectoId;
    private UUID empleadoId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/rrhh/costos";
        asistenciaRegistroJpaRepository.deleteAll();
        historialLaboralJpaRepository.deleteAll();
        empleadoJpaRepository.deleteAll();
        proyectoJpaRepository.deleteAll();

        // 1. Project
        ProyectoEntity proyecto = new ProyectoEntity();
        proyectoId = UUID.randomUUID();
        proyecto.setId(proyectoId);
        proyecto.setNombre("Cost Project");
        proyecto.setUbicacion("Office");
        proyecto.setCreatedBy(UUID.randomUUID());
        proyecto = proyectoJpaRepository.save(proyecto);

        // 2. Empleado
        EmpleadoEntity empleado = new EmpleadoEntity();
        empleadoId = UUID.randomUUID();
        empleado.setId(empleadoId);
        empleado.setNombre("Cost");
        empleado.setApellido("User");
        empleado.setNumeroIdentificacion("COST123");
        empleado.setEstado(com.budgetpro.domain.rrhh.model.EstadoEmpleado.ACTIVO);
        empleado.setCreatedBy(UUID.randomUUID());
        empleado = empleadoJpaRepository.save(empleado);

        // 3. Historial (Required for salary lookup in UseCase)
        HistorialLaboralEntity hist = new HistorialLaboralEntity();
        hist.setId(UUID.randomUUID());
        hist.setEmpleado(empleado);
        hist.setSalarioBase(new BigDecimal("2400.00")); // 80 per day -> 10 per hour
        hist.setCargo("Admin");
        hist.setTipoEmpleado(TipoEmpleado.PERMANENTE);
        hist.setFechaInicio(LocalDate.now().minusMonths(2));
        hist.setCreatedBy(UUID.randomUUID());
        historialLaboralJpaRepository.save(hist);

        // 4. Attendance (8 hours work)
        AsistenciaRegistroEntity asist = new AsistenciaRegistroEntity();
        asist.setId(UUID.randomUUID());
        asist.setEmpleado(empleado);
        asist.setProyecto(proyecto);
        asist.setFecha(LocalDate.now().minusDays(1));
        asist.setHoraEntrada(java.time.LocalTime.of(8, 0));
        asist.setHoraSalida(java.time.LocalTime.of(16, 0));
        asist.setHorasTrabajadas(new BigDecimal("8.0"));
        asist.setHorasExtras(BigDecimal.ZERO);
        asist.setEstado(com.budgetpro.domain.rrhh.model.EstadoAsistencia.PRESENTE);
        asist.setCreatedBy(UUID.randomUUID());
        asistenciaRegistroJpaRepository.save(asist);
    }

    @Test
    void testConsultarCostos_HappyPath() {
        String url = String.format("%s?proyectoId=%s&fechaInicio=%s&fechaFin=%s", baseUrl, proyectoId,
                LocalDate.now().minusDays(2), LocalDate.now());

        ResponseEntity<CostosLaboralesResponse> response = restTemplate.getForEntity(url,
                CostosLaboralesResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // 8 hours * 10/hour = 80
        assertThat(response.getBody().getTotalCosto()).isEqualByComparingTo("80.00");
        assertThat(response.getBody().getDesglose()).isNotEmpty();
    }

    @Test
    void testConsultarCostos_WithVarianza() {
        String url = String.format("%s?proyectoId=%s&fechaInicio=%s&fechaFin=%s&incluirVarianza=true", baseUrl,
                proyectoId, LocalDate.now().minusDays(2), LocalDate.now());

        ResponseEntity<CostosLaboralesResponse> response = restTemplate.getForEntity(url,
                CostosLaboralesResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getVarianza()).isPresent();
    }

    @Test
    void testConsultarCostos_GroupingByEmpleado() {
        String url = String.format("%s?proyectoId=%s&fechaInicio=%s&fechaFin=%s&agruparPor=EMPLEADO", baseUrl,
                proyectoId, LocalDate.now().minusDays(2), LocalDate.now());

        ResponseEntity<CostosLaboralesResponse> response = restTemplate.getForEntity(url,
                CostosLaboralesResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDesglose().get(0).getNombreGrupo()).contains("Cost User");
    }
}
