package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.recurso.port.out.RecursoRepository;
import com.budgetpro.application.rrhh.dto.AsignarEmpleadoProyectoCommand;
import com.budgetpro.application.rrhh.dto.RegistrarAsistenciaCommand;
import com.budgetpro.application.rrhh.exception.ProyectoNoActivoException;
import com.budgetpro.application.rrhh.port.out.AsignacionProyectoRepositoryPort;
import com.budgetpro.application.rrhh.port.out.AsistenciaRepositoryPort;
import com.budgetpro.domain.rrhh.port.AsignacionSolapeValidator;
import com.budgetpro.domain.rrhh.service.NoOpAsignacionSolapeValidator;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.application.rrhh.port.out.ProyectoRepositoryPort;
import com.budgetpro.domain.catalogo.port.RecursoProxyRepository;
import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import com.budgetpro.domain.rrhh.model.Contacto;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.TipoEmpleado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RrhhRegla150ProyectoActivoTest {

    @Mock
    private EmpleadoRepositoryPort empleadoRepositoryPort;

    @Mock
    private ProyectoRepositoryPort rrhhProyectoRepositoryPort;

    @Mock
    private AsistenciaRepositoryPort asistenciaRepositoryPort;

    @Mock
    private EmpleadoRepositoryPort empleadoRepositoryAsignar;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private RecursoRepository recursoCatalogRepository;

    @Mock
    private RecursoProxyRepository recursoProxyRepository;

    @Mock
    private AsignacionProyectoRepositoryPort asignacionRepository;

    private final AsignacionSolapeValidator asignacionSolapeValidator = new NoOpAsignacionSolapeValidator();

    private RegistrarAsistenciaUseCaseImpl registrarAsistencia;
    private AsignarEmpleadoProyectoUseCaseImpl asignarEmpleado;

    private final UUID empleadoUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID proyectoUuid = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @BeforeEach
    void setUp() {
        registrarAsistencia = new RegistrarAsistenciaUseCaseImpl(empleadoRepositoryPort, rrhhProyectoRepositoryPort,
                asistenciaRepositoryPort, asignacionRepository, asignacionSolapeValidator);
        asignarEmpleado = new AsignarEmpleadoProyectoUseCaseImpl(empleadoRepositoryAsignar, proyectoRepository,
                recursoCatalogRepository, recursoProxyRepository, asignacionRepository);
    }

    @Test
    void registrarAsistencia_proyectoSuspendido_lanzaProyectoNoActivoException() {
        Empleado empleado = empleadoActivo(empleadoUuid);
        when(empleadoRepositoryPort.findById(EmpleadoId.of(empleadoUuid))).thenReturn(Optional.of(empleado));
        when(rrhhProyectoRepositoryPort.findById(ProyectoId.from(proyectoUuid)))
                .thenReturn(Optional.of(Proyecto.reconstruir(ProyectoId.from(proyectoUuid), "Obra", "Lima",
                        EstadoProyecto.SUSPENDIDO)));

        RegistrarAsistenciaCommand command = new RegistrarAsistenciaCommand(EmpleadoId.of(empleadoUuid),
                ProyectoId.from(proyectoUuid), LocalDate.of(2025, 4, 1), LocalDateTime.of(2025, 4, 1, 8, 0),
                LocalDateTime.of(2025, 4, 1, 17, 0), null);

        assertThrows(ProyectoNoActivoException.class, () -> registrarAsistencia.registrarAsistencia(command));
        verify(asistenciaRepositoryPort, never()).findOverlapping(any(), any(), any(), any());
    }

    @Test
    void asignarEmpleado_proyectoBorrador_lanzaProyectoNoActivoException() {
        when(empleadoRepositoryAsignar.findById(EmpleadoId.of(empleadoUuid)))
                .thenReturn(Optional.of(empleadoActivo(empleadoUuid)));
        when(proyectoRepository.findById(ProyectoId.from(proyectoUuid)))
                .thenReturn(Optional.of(Proyecto.reconstruir(ProyectoId.from(proyectoUuid), "Obra", "Lima",
                        EstadoProyecto.BORRADOR)));

        AsignarEmpleadoProyectoCommand command = new AsignarEmpleadoProyectoCommand(empleadoUuid, proyectoUuid,
                LocalDate.of(2025, 4, 1), LocalDate.of(2025, 12, 31), new BigDecimal("40"), "OPERARIO");

        assertThrows(ProyectoNoActivoException.class, () -> asignarEmpleado.asignar(command));
        verify(asignacionRepository, never()).save(any());
    }

    private static Empleado empleadoActivo(UUID id) {
        Contacto contacto = Contacto.of("e@test.com", null, null);
        return Empleado.crear(EmpleadoId.of(id), "Juan", "Pérez", "DOC-1", contacto, LocalDate.of(2024, 1, 1),
                new BigDecimal("3000"), "Oficial", TipoEmpleado.PERMANENTE);
    }
}
