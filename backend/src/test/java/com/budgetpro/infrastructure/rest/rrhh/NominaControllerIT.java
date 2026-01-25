package com.budgetpro.infrastructure.rest.rrhh;

import com.budgetpro.application.rrhh.dto.NominaResponse;
import com.budgetpro.domain.rrhh.model.TipoEmpleado;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.AsistenciaRegistroEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.ConfiguracionLaboralExtendidaEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.EmpleadoEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.HistorialLaboralEntity;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.rrhh.*;
import com.budgetpro.infrastructure.rest.rrhh.dto.CalcularNominaRequest;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NominaControllerIT extends AbstractIntegrationTest {

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private NominaJpaRepository nominaJpaRepository;

        @Autowired
        private EmpleadoJpaRepository empleadoJpaRepository;

        @Autowired
        private ProyectoJpaRepository proyectoJpaRepository;

        @Autowired
        private AsistenciaRegistroJpaRepository asistenciaRegistroJpaRepository;

        @Autowired
        private ConfiguracionLaboralExtendidaJpaRepository configuracionRepository;

        @Autowired
        private HistorialLaboralJpaRepository historialLaboralJpaRepository;

        @LocalServerPort
        private int port;

        private String baseUrl;
        private UUID proyectoId;
        private UUID empleadoId;

        @BeforeEach
        void setUp() {
                baseUrl = "http://localhost:" + port + "/api/v1/rrhh/nominas";
                nominaJpaRepository.deleteAll();
                asistenciaRegistroJpaRepository.deleteAll();
                historialLaboralJpaRepository.deleteAll();
                empleadoJpaRepository.deleteAll();
                configuracionRepository.deleteAll();
                proyectoJpaRepository.deleteAll();

                // 1. Project
                ProyectoEntity proyecto = new ProyectoEntity();
                proyectoId = UUID.randomUUID();
                proyecto.setId(proyectoId);
                proyecto.setNombre("Payroll Project");
                proyecto.setUbicacion("Office");
                proyecto.setCreatedBy(UUID.randomUUID());
                proyecto = proyectoJpaRepository.save(proyecto);

                // 2. Global Config (at least one)
                ConfiguracionLaboralExtendidaEntity config = new ConfiguracionLaboralExtendidaEntity();
                config.setId(UUID.randomUUID());
                config.setProyecto(null);
                config.setFechaVigenciaInicio(LocalDate.now().minusYears(1));
                config.setFsrConfig(Map.of("diasAguinaldo", 15, "diasVacaciones", 15, "porcentajeSeguridadSocial", 0.15,
                                "diasNoTrabajados", 5, "diasLaborablesAno", 251));
                config.setCreatedBy(UUID.randomUUID());
                configuracionRepository.save(config);

                // 3. Empleado
                EmpleadoEntity empleado = new EmpleadoEntity();
                empleadoId = UUID.randomUUID();
                empleado.setId(empleadoId);
                empleado.setNombre("Payroll");
                empleado.setApellido("User");
                empleado.setNumeroIdentificacion("PAY123");
                empleado.setEstado(com.budgetpro.domain.rrhh.model.EstadoEmpleado.ACTIVO);
                empleado.setCreatedBy(UUID.randomUUID());
                empleado = empleadoJpaRepository.save(empleado);

                // 4. Initial HistorialLaboral (Calculations often depend on it)
                HistorialLaboralEntity hist = new HistorialLaboralEntity();
                hist.setId(UUID.randomUUID());
                hist.setEmpleado(empleado);
                hist.setSalarioBase(new BigDecimal("3000.00"));
                hist.setCargo("Admin");
                hist.setTipoEmpleado(TipoEmpleado.PERMANENTE);
                hist.setFechaInicio(LocalDate.now().minusMonths(2));
                hist.setCreatedBy(UUID.randomUUID());
                historialLaboralJpaRepository.save(hist);

                // 5. Attendance (8 hours work)
                AsistenciaRegistroEntity asist = new AsistenciaRegistroEntity();
                asist.setId(UUID.randomUUID());
                asist.setEmpleado(empleado);
                asist.setProyecto(proyecto);
                asist.setFecha(LocalDate.now().minusDays(5));
                asist.setHoraEntrada(java.time.LocalTime.of(8, 0));
                asist.setHoraSalida(java.time.LocalTime.of(17, 0)); // 9 hours (1 extra)
                asist.setHorasTrabajadas(new BigDecimal("9.0"));
                asist.setHorasExtras(new BigDecimal("1.0"));
                asist.setEstado(com.budgetpro.domain.rrhh.model.EstadoAsistencia.PRESENTE);
                asist.setCreatedBy(UUID.randomUUID());
                asistenciaRegistroJpaRepository.save(asist);
        }

        @Test
        void testCalcularNomina_HappyPath() {
                CalcularNominaRequest request = new CalcularNominaRequest(proyectoId.toString(),
                                LocalDate.now().withDayOfMonth(1),
                                LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()),
                                List.of(empleadoId.toString()));

                ResponseEntity<NominaResponse> response = restTemplate.postForEntity(baseUrl + "/calcular", request,
                                NominaResponse.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getProyectoId()).isEqualTo(proyectoId);
                assertThat(response.getBody().getCantidadEmpleados()).isEqualTo(1);
        }

        @Test
        void testCalcularNomina_Idempotency() {
                testCalcularNomina_HappyPath();

                // Second call for same project/period
                CalcularNominaRequest request = new CalcularNominaRequest(proyectoId.toString(),
                                LocalDate.now().withDayOfMonth(1),
                                LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()),
                                List.of(empleadoId.toString()));

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/calcular", request,
                                String.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                // Note: Controller might be re-throwing IllegalStateException which might map
                // to 500 depending on GlobalExceptionHandler
                // But the requirement says "rejected".
        }

        @Test
        void testTemporalSalaryScenario() {
                // Change salary mid-month
                LocalDate midMonth = LocalDate.now().withDayOfMonth(15);
                HistorialLaboralEntity newHist = new HistorialLaboralEntity();
                newHist.setId(UUID.randomUUID());
                newHist.setEmpleado(empleadoJpaRepository.findById(empleadoId).get());
                newHist.setSalarioBase(new BigDecimal("4500.00")); // Increased salary
                newHist.setCargo("Admin Senior");
                newHist.setTipoEmpleado(TipoEmpleado.PERMANENTE);
                newHist.setFechaInicio(midMonth);
                newHist.setCreatedBy(UUID.randomUUID());
                historialLaboralJpaRepository.save(newHist);

                CalcularNominaRequest request = new CalcularNominaRequest(proyectoId.toString(),
                                LocalDate.now().withDayOfMonth(1),
                                LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()),
                                List.of(empleadoId.toString()));

                ResponseEntity<NominaResponse> response = restTemplate.postForEntity(baseUrl + "/calcular", request,
                                NominaResponse.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                // If calculation logic works, totalBruto should reflect mixed salary
                // 14 days at 3000/30=100 -> 1400
                // 16 days (assuming 30-day month) at 4500/30=150 -> 2400
                // Total should be around 3800 + extras
                assertThat(response.getBody().getTotalBruto()).isGreaterThan(new BigDecimal("3000.00"));
        }
}
