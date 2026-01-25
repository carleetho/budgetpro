package com.budgetpro.infrastructure.rest.rrhh;

import com.budgetpro.application.rrhh.dto.CuadrillaResponse;
import com.budgetpro.domain.rrhh.model.EstadoCuadrilla;
import com.budgetpro.domain.rrhh.model.TipoEmpleado;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.EmpleadoEntity;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.rrhh.CuadrillaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.rrhh.EmpleadoJpaRepository;
import com.budgetpro.infrastructure.rest.rrhh.dto.ActualizarMiembrosRequest;
import com.budgetpro.infrastructure.rest.rrhh.dto.AsignarActividadRequest;
import com.budgetpro.infrastructure.rest.rrhh.dto.CrearCuadrillaRequest;
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

class CuadrillaControllerIT extends AbstractIntegrationTest {

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private CuadrillaJpaRepository cuadrillaJpaRepository;

        @Autowired
        private EmpleadoJpaRepository empleadoJpaRepository;

        @Autowired
        private ProyectoJpaRepository proyectoJpaRepository;

        @Autowired
        private PartidaJpaRepository partidaJpaRepository;

        @Autowired
        private PresupuestoJpaRepository presupuestoJpaRepository;

        @LocalServerPort
        private int port;

        private String baseUrl;
        private UUID proyectoId;
        private UUID liderId;
        private UUID miembroId;

        @BeforeEach
        void setUp() {
                baseUrl = "http://localhost:" + port + "/api/v1/rrhh/cuadrillas";
                cuadrillaJpaRepository.deleteAll();
                empleadoJpaRepository.deleteAll();
                partidaJpaRepository.deleteAll();
                proyectoJpaRepository.deleteAll();

                // Setup Proyecto
                ProyectoEntity proyecto = new ProyectoEntity();
                proyectoId = UUID.randomUUID();
                proyecto.setId(proyectoId);
                proyecto.setNombre("Crew Project");
                proyecto.setUbicacion("Site 1");
                proyecto.setCreatedBy(UUID.randomUUID());
                proyectoJpaRepository.save(proyecto);

                // Setup Empleados
                liderId = createEmpleado("Lider", "ID-L");
                miembroId = createEmpleado("Miembro", "ID-M");
        }

        private UUID createEmpleado(String nombre, String numId) {
                EmpleadoEntity e = new EmpleadoEntity();
                UUID id = UUID.randomUUID();
                e.setId(id);
                e.setNombre(nombre);
                e.setApellido("Test");
                e.setNumeroIdentificacion(numId);
                e.setEstado(com.budgetpro.domain.rrhh.model.EstadoEmpleado.ACTIVO);
                e.setCreatedBy(UUID.randomUUID());
                return empleadoJpaRepository.save(e).getId();
        }

        @Test
        void testCrearCuadrilla_HappyPath() {
                CrearCuadrillaRequest request = new CrearCuadrillaRequest(proyectoId.toString(), "Cuadrilla Alfa",
                                "OBRA_CIVIL", liderId.toString(), List.of(miembroId.toString()));

                ResponseEntity<CuadrillaResponse> response = restTemplate.postForEntity(baseUrl, request,
                                CuadrillaResponse.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().nombre()).isEqualTo("Cuadrilla Alfa");
                assertThat(response.getBody().estado()).isEqualTo(EstadoCuadrilla.ACTIVA);
        }

        @Test
        void testActualizarMiembros() {
                // Create first
                testCrearCuadrilla_HappyPath();
                ResponseEntity<CuadrillaResponse[]> listResponse = restTemplate.getForEntity(baseUrl,
                                CuadrillaResponse[].class);
                UUID cuadrillaId = listResponse.getBody()[0].id();

                UUID otroMiembroId = createEmpleado("Otro", "ID-O");
                ActualizarMiembrosRequest request = new ActualizarMiembrosRequest(null, otroMiembroId.toString(), null);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ActualizarMiembrosRequest> entity = new HttpEntity<>(request, headers);

                ResponseEntity<CuadrillaResponse> response = restTemplate.exchange(
                                baseUrl + "/" + cuadrillaId + "/miembros", HttpMethod.PUT, entity,
                                CuadrillaResponse.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().miembrosIds()).contains(otroMiembroId);
        }

        @Test
        void testAsignarActividad() {
                testCrearCuadrilla_HappyPath();
                ResponseEntity<CuadrillaResponse[]> listResponse = restTemplate.getForEntity(baseUrl,
                                CuadrillaResponse[].class);
                UUID cuadrillaId = listResponse.getBody()[0].id();

                // Create Presupuesto (Required by PartidaEntity)
                PresupuestoEntity presupuesto = new PresupuestoEntity();
                presupuesto.setId(UUID.randomUUID());
                presupuesto.setNombre("Test Presupuesto");
                presupuesto.setProyecto(proyectoJpaRepository.findById(proyectoId).get());
                presupuesto.setEstado(com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto.BORRADOR);
                presupuesto.setCreatedBy(UUID.randomUUID());
                presupuesto = presupuestoJpaRepository.save(presupuesto);

                // Create Partida
                PartidaEntity partida = new PartidaEntity();
                UUID partidaId = UUID.randomUUID();
                partida.setId(partidaId);
                partida.setPresupuesto(presupuesto);
                partida.setCodigo("P-001");
                partida.setDescripcion("Construccion Muro");
                partida.setMetradoOriginal(new BigDecimal("100"));
                partida.setMetradoVigente(new BigDecimal("100"));
                partida.setPrecioUnitario(new BigDecimal("50"));
                partida.setGastosReales(BigDecimal.ZERO);
                partida.setCompromisosPendientes(BigDecimal.ZERO);
                partida.setCreatedBy(UUID.randomUUID());
                partidaJpaRepository.save(partida);

                AsignarActividadRequest request = new AsignarActividadRequest(proyectoId.toString(),
                                partidaId.toString(), LocalDate.now(), LocalDate.now().plusDays(5));

                ResponseEntity<Void> response = restTemplate.postForEntity(baseUrl + "/" + cuadrillaId + "/actividades",
                                request, Void.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
}
