package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.recurso.port.out.RecursoRepository;
import com.budgetpro.application.rrhh.dto.AsignarEmpleadoProyectoCommand;
import com.budgetpro.application.rrhh.port.out.AsignacionProyectoRepositoryPort;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.domain.catalogo.model.RecursoProxyId;
import com.budgetpro.domain.catalogo.port.RecursoProxyRepository;
import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import com.budgetpro.domain.rrhh.exception.AsignacionSuperpuestaException;
import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.AsignacionProyectoId;
import com.budgetpro.domain.rrhh.model.Contacto;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.TipoEmpleado;
import com.budgetpro.domain.rrhh.service.RegimenCivilSolapeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsignarEmpleadoProyectoUseCaseSolapeTest {

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

    private AsignarEmpleadoProyectoUseCaseImpl asignarEmpleado;

    private final UUID empleadoUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID proyectoUuid = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private final UUID otroProyectoUuid = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @BeforeEach
    void setUp() {
        asignarEmpleado = new AsignarEmpleadoProyectoUseCaseImpl(empleadoRepository, proyectoRepository,
                recursoCatalogRepository, recursoProxyRepository, asignacionRepository, new RegimenCivilSolapeValidator());
    }

    @Test
    @DisplayName("asignar: solape de intervalos con asignación existente → AsignacionSuperpuestaException (409 ASIGNACION_PROYECTO_CONFLICTO vía handler)")
    void asignar_solapeExistente_lanzaAsignacionSuperpuestaException() {
        when(empleadoRepository.findById(EmpleadoId.of(empleadoUuid)))
                .thenReturn(Optional.of(empleadoActivo(empleadoUuid)));
        when(proyectoRepository.findById(ProyectoId.from(proyectoUuid)))
                .thenReturn(Optional.of(Proyecto.reconstruir(ProyectoId.from(proyectoUuid), "Obra", "Lima",
                        EstadoProyecto.ACTIVO)));

        AsignacionProyecto existente = AsignacionProyecto.reconstruir(AsignacionProyectoId.generate(),
                EmpleadoId.of(empleadoUuid), ProyectoId.from(otroProyectoUuid), RecursoProxyId.generate(),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30), null, null);
        when(asignacionRepository.findAsignacionesByEmpleadoId(EmpleadoId.of(empleadoUuid)))
                .thenReturn(List.of(existente));

        AsignarEmpleadoProyectoCommand command = new AsignarEmpleadoProyectoCommand(empleadoUuid, proyectoUuid,
                LocalDate.of(2025, 4, 1), LocalDate.of(2025, 12, 31), new BigDecimal("40"), "OPERARIO");

        assertThrows(AsignacionSuperpuestaException.class, () -> asignarEmpleado.asignar(command));
        verify(recursoProxyRepository, never()).save(any());
        verify(asignacionRepository, never()).save(any());
    }

    private static Empleado empleadoActivo(UUID id) {
        Contacto contacto = Contacto.of("e@test.com", null, null);
        return Empleado.crear(EmpleadoId.of(id), "Juan", "Pérez", "DOC-1", contacto, LocalDate.of(2024, 1, 1),
                new BigDecimal("3000"), "Oficial", TipoEmpleado.PERMANENTE);
    }
}
