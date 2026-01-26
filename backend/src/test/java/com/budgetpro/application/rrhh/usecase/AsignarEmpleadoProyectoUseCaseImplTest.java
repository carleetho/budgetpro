package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.recurso.port.out.RecursoRepository;
import com.budgetpro.application.rrhh.dto.AsignacionProyectoResponse;
import com.budgetpro.application.rrhh.dto.AsignarEmpleadoProyectoCommand;
import com.budgetpro.application.rrhh.exception.AsignacionProyectoConflictoException;
import com.budgetpro.application.rrhh.port.out.AsignacionProyectoRepositoryPort;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.domain.catalogo.model.RecursoProxy;
import com.budgetpro.domain.catalogo.port.RecursoProxyRepository;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import com.budgetpro.domain.recurso.model.Recurso;
import com.budgetpro.domain.recurso.model.RecursoId;
import com.budgetpro.domain.shared.model.TipoRecurso;
import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignarEmpleadoProyectoUseCaseImplTest {

        @Mock
        private EmpleadoRepositoryPort empleadoRepository;
        @Mock
        private ProyectoRepository proyectoRepository;
        @Mock
        private RecursoRepository recursoCatalogRepository;
        @Mock
        private RecursoProxyRepository recursoProxyRepository;
        @Mock
        private AsignacionProyectoRepositoryPort asignacionRepository;

        @InjectMocks
        private AsignarEmpleadoProyectoUseCaseImpl useCase;

        private UUID empleadoId;
        private UUID proyectoId;
        private Empleado empleado;
        private Proyecto proyecto;
        private Recurso recurso;

        @BeforeEach
        void setUp() {
                empleadoId = UUID.randomUUID();
                proyectoId = UUID.randomUUID();

                empleado = Empleado.crear(EmpleadoId.of(empleadoId), "Juan", "Perez", "12345678", null, LocalDate.now(),
                                new BigDecimal("100.00"), "Oficial Albañil",
                                com.budgetpro.domain.rrhh.model.TipoEmpleado.PERMANENTE);

                proyecto = Proyecto.crear(ProyectoId.from(proyectoId), "Edificio A", "Lima");

                recurso = Recurso.crear(RecursoId.generate(), "OFICIAL ALBAÑIL", TipoRecurso.MANO_OBRA, "HR");
        }

        @Test
        void deberiaAsignarEmpleadoExitosamente() {
                // Given
                AsignarEmpleadoProyectoCommand command = new AsignarEmpleadoProyectoCommand(empleadoId, proyectoId,
                                LocalDate.now(), LocalDate.now().plusMonths(1), null, "Lider de Cuadrilla");

                when(empleadoRepository.findById(EmpleadoId.of(empleadoId))).thenReturn(Optional.of(empleado));
                when(proyectoRepository.findById(ProyectoId.from(proyectoId))).thenReturn(Optional.of(proyecto));
                when(asignacionRepository.existsOverlap(any(), any(), any())).thenReturn(false);
                when(recursoCatalogRepository.findByNombre("OFICIAL ALBAÑIL")).thenReturn(Optional.of(recurso));

                // When
                AsignacionProyectoResponse response = useCase.asignar(command);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.empleadoId()).isEqualTo(empleadoId);
                assertThat(response.proyectoId()).isEqualTo(proyectoId);
                assertThat(response.rolProyecto()).isEqualTo("Lider de Cuadrilla");

                verify(recursoProxyRepository).save(any(RecursoProxy.class));
                verify(asignacionRepository).save(any(AsignacionProyecto.class));
        }

        @Test
        void deberiaLanzarExcepcionSiEmpleadoNoExiste() {
                // Given
                AsignarEmpleadoProyectoCommand command = new AsignarEmpleadoProyectoCommand(empleadoId, proyectoId,
                                LocalDate.now(), null, null, null);

                when(empleadoRepository.findById(EmpleadoId.of(empleadoId))).thenReturn(Optional.empty());

                // When / Then
                assertThatThrownBy(() -> useCase.asignar(command)).isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Empleado no encontrado");
        }

        @Test
        void deberiaLanzarExcepcionSiEmpleadoNoEstaActivo() {
                // Given
                empleado.inactivar(LocalDate.now());
                AsignarEmpleadoProyectoCommand command = new AsignarEmpleadoProyectoCommand(empleadoId, proyectoId,
                                LocalDate.now(), null, null, null);

                when(empleadoRepository.findById(EmpleadoId.of(empleadoId))).thenReturn(Optional.of(empleado));

                // When / Then
                assertThatThrownBy(() -> useCase.asignar(command)).isInstanceOf(IllegalStateException.class)
                                .hasMessageContaining("El empleado no está activo");
        }

        @Test
        void deberiaLanzarExcepcionSiProyectoNoExiste() {
                // Given
                AsignarEmpleadoProyectoCommand command = new AsignarEmpleadoProyectoCommand(empleadoId, proyectoId,
                                LocalDate.now(), null, null, null);

                when(empleadoRepository.findById(EmpleadoId.of(empleadoId))).thenReturn(Optional.of(empleado));
                when(proyectoRepository.findById(ProyectoId.from(proyectoId))).thenReturn(Optional.empty());

                // When / Then
                assertThatThrownBy(() -> useCase.asignar(command)).isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Proyecto no encontrado");
        }

        @Test
        void deberiaLanzarExcepcionSiHaySolapamiento() {
                // Given
                AsignarEmpleadoProyectoCommand command = new AsignarEmpleadoProyectoCommand(empleadoId, proyectoId,
                                LocalDate.now(), LocalDate.now().plusDays(10), null, null);

                when(empleadoRepository.findById(EmpleadoId.of(empleadoId))).thenReturn(Optional.of(empleado));
                when(proyectoRepository.findById(ProyectoId.from(proyectoId))).thenReturn(Optional.of(proyecto));
                when(asignacionRepository.existsOverlap(any(), any(), any())).thenReturn(true);

                // When / Then
                assertThatThrownBy(() -> useCase.asignar(command))
                                .isInstanceOf(AsignacionProyectoConflictoException.class);
        }

        @Test
        void deberiaLanzarExcepcionSiNoHayRecursoEnCatalogo() {
                // Given
                AsignarEmpleadoProyectoCommand command = new AsignarEmpleadoProyectoCommand(empleadoId, proyectoId,
                                LocalDate.now(), null, null, null);

                when(empleadoRepository.findById(EmpleadoId.of(empleadoId))).thenReturn(Optional.of(empleado));
                when(proyectoRepository.findById(ProyectoId.from(proyectoId))).thenReturn(Optional.of(proyecto));
                when(asignacionRepository.existsOverlap(any(), any(), any())).thenReturn(false);
                when(recursoCatalogRepository.findByNombre("OFICIAL ALBAÑIL")).thenReturn(Optional.empty());

                // When / Then
                assertThatThrownBy(() -> useCase.asignar(command)).isInstanceOf(IllegalStateException.class)
                                .hasMessageContaining("No se encontró el recurso en el catálogo");
        }
}
