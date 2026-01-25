package com.budgetpro.infrastructure.rest.rrhh;

import com.budgetpro.application.rrhh.dto.AsistenciaResponse;
import com.budgetpro.application.rrhh.dto.ResumenAsistenciaResponse;
import com.budgetpro.domain.rrhh.model.TipoEmpleado;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.EmpleadoEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.HistorialLaboralEntity;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.rrhh.AsistenciaRegistroJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.rrhh.EmpleadoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.rrhh.HistorialLaboralJpaRepository;
import com.budgetpro.infrastructure.rest.rrhh.dto.RegistrarAsistenciaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AsistenciaControllerIT extends AbstractIntegrationTest {

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
        private UUID empleadoId;
        private UUID proyectoId;

        @BeforeEach
        void setUp() {
                baseUrl = "http://localhost:" + port + "/api/v1/rrhh/asistencias";
                asistenciaRegistroJpaRepository.deleteAll();
                empleadoJpaRepository.deleteAll();
                proyectoJpaRepository.deleteAll();

                EmpleadoEntity empleado = new EmpleadoEntity();
                empleadoId = UUID.randomUUID();
                empleado.setId(empleadoId);
                empleado.setNombre("Test");
                empleado.setApellido("User");
                empleado.setNumeroIdentificacion("ID123");
                empleado.setEstado(com.budgetpro.domain.rrhh.model.EstadoEmpleado.ACTIVO);
                empleado.setCreatedBy(UUID.randomUUID());
                empleado = empleadoJpaRepository.save(empleado);

                // Create initial history
                HistorialLaboralEntity history = new HistorialLaboralEntity();
                history.setId(UUID.randomUUID());
                history.setEmpleado(empleado);
                history.setCargo("Worker");
                history.setSalarioBase(new BigDecimal("100.00"));
                history.setTipoEmpleado(TipoEmpleado.PERMANENTE);
                history.setFechaInicio(LocalDate.now().minusMonths(1));
                history.setCreatedBy(UUID.randomUUID());
                historialLaboralJpaRepository.save(history); // Added to avoid missing info in responses if any

                // Create test project
                ProyectoEntity proyecto = new ProyectoEntity();
                proyectoId = UUID.randomUUID();
                proyecto.setId(proyectoId);
                proyecto.setNombre("Project X");
                proyecto.setUbicacion("Somewhere");
                proyecto.setCreatedBy(UUID.randomUUID());
                proyecto = proyectoJpaRepository.save(proyecto);
        }

        @Test
        void testRegistrarAsistencia_HappyPath() {
                LocalDate fecha = LocalDate.now();
                LocalDateTime entrada = fecha.atTime(8, 0);
                LocalDateTime salida = fecha.atTime(17, 0);

                RegistrarAsistenciaRequest request = new RegistrarAsistenciaRequest(empleadoId.toString(),
                                proyectoId.toString(), fecha, entrada, salida, "Site A");

                ResponseEntity<AsistenciaResponse> response = restTemplate.postForEntity(baseUrl, request,
                                AsistenciaResponse.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getHorasTrabajadas()).isEqualTo(9.0); // 8:00 to 17:00 is 9 hours
        }

        @Test
        void testRegistrarAsistencia_OvernightShift() {
                LocalDate fecha = LocalDate.now().minusDays(1);
                LocalDateTime entrada = fecha.atTime(22, 0);
                LocalDateTime salida = fecha.plusDays(1).atTime(6, 0);

                RegistrarAsistenciaRequest request = new RegistrarAsistenciaRequest(empleadoId.toString(),
                                proyectoId.toString(), fecha, entrada, salida, "Site A");

                ResponseEntity<AsistenciaResponse> response = restTemplate.postForEntity(baseUrl, request,
                                AsistenciaResponse.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                assertThat(response.getBody().getHorasTrabajadas()).isEqualTo(8.0);
        }

        @Test
        void testRegistrarAsistencia_Overlap() {
                LocalDate fecha = LocalDate.now();
                LocalDateTime entrada1 = fecha.atTime(8, 0);
                LocalDateTime salida1 = fecha.atTime(12, 0);

                RegistrarAsistenciaRequest request1 = new RegistrarAsistenciaRequest(empleadoId.toString(),
                                proyectoId.toString(), fecha, entrada1, salida1, "Site A");
                restTemplate.postForEntity(baseUrl, request1, AsistenciaResponse.class);

                // Overlapping shift
                LocalDateTime entrada2 = fecha.atTime(10, 0);
                LocalDateTime salida2 = fecha.atTime(14, 0);
                RegistrarAsistenciaRequest request2 = new RegistrarAsistenciaRequest(empleadoId.toString(),
                                proyectoId.toString(), fecha, entrada2, salida2, "Site A");

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request2, String.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void testResumenMensual() {
                // Register some attendance
                testRegistrarAsistencia_HappyPath();

                LocalDate fecha = LocalDate.now();
                String url = String.format("%s/resumen?empleadoId=%s&mes=%d&ano=%d", baseUrl, empleadoId,
                                fecha.getMonthValue(), fecha.getYear());

                ResponseEntity<ResumenAsistenciaResponse> response = restTemplate.getForEntity(url,
                                ResumenAsistenciaResponse.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getTotalHorasTrabajadas()).isGreaterThan(0);
        }
}
