package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.RegistrarAsistenciaCommand;
import com.budgetpro.application.rrhh.exception.AsistenciaSuperpuestaException;
import com.budgetpro.application.rrhh.exception.ProyectoNoActivoException;
import com.budgetpro.application.rrhh.port.out.AsignacionProyectoRepositoryPort;
import com.budgetpro.application.rrhh.port.out.AsistenciaRepositoryPort;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.application.rrhh.port.out.ProyectoRepositoryPort;
import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.exception.InactiveWorkerException;
import com.budgetpro.domain.rrhh.exception.TrabajadorNoAsignadoAlProyectoException;
import com.budgetpro.domain.rrhh.model.AsistenciaId;
import com.budgetpro.domain.rrhh.model.AsistenciaRegistro;
import com.budgetpro.domain.rrhh.model.Contacto;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.TipoEmpleado;
import com.budgetpro.domain.rrhh.port.AsignacionSolapeValidator;
import com.budgetpro.domain.rrhh.service.NoOpAsignacionSolapeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarAsistenciaUseCaseImplTest {

    @Mock
    private EmpleadoRepositoryPort empleadoRepositoryPort;

    @Mock
    private ProyectoRepositoryPort proyectoRepositoryPort;

    @Mock
    private AsistenciaRepositoryPort asistenciaRepositoryPort;

    @Mock
    private AsignacionProyectoRepositoryPort asignacionProyectoRepositoryPort;

    private final AsignacionSolapeValidator asignacionSolapeValidator = new NoOpAsignacionSolapeValidator();

    private RegistrarAsistenciaUseCaseImpl useCase;

    private final UUID empleadoUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID proyectoUuid = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @BeforeEach
    void setUp() {
        useCase = new RegistrarAsistenciaUseCaseImpl(empleadoRepositoryPort, proyectoRepositoryPort,
                asistenciaRepositoryPort, asignacionProyectoRepositoryPort, asignacionSolapeValidator);
    }

    @Test
    @DisplayName("flujo feliz: empleado activo, proyecto ACTIVO, asignación vigente, sin solapes")
    void registroFeliz_persiste() {
        LocalDate fecha = LocalDate.of(2025, 4, 1);
        when(empleadoRepositoryPort.findById(EmpleadoId.of(empleadoUuid))).thenReturn(Optional.of(empleadoActivo()));
        when(proyectoRepositoryPort.findById(ProyectoId.from(proyectoUuid)))
                .thenReturn(Optional.of(Proyecto.reconstruir(ProyectoId.from(proyectoUuid), "Obra", "Lima",
                        EstadoProyecto.ACTIVO)));
        when(asignacionProyectoRepositoryPort.existsVigenteAsignacionEmpleadoProyectoEnFecha(
                EmpleadoId.of(empleadoUuid), ProyectoId.from(proyectoUuid), fecha)).thenReturn(true);
        when(asistenciaRepositoryPort.findOverlapping(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        AsistenciaRegistro guardado = AsistenciaRegistro.registrar(AsistenciaId.random(), EmpleadoId.of(empleadoUuid),
                ProyectoId.from(proyectoUuid), fecha, java.time.LocalTime.of(8, 0), java.time.LocalTime.of(17, 0),
                null);
        when(asistenciaRepositoryPort.save(any(AsistenciaRegistro.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistrarAsistenciaCommand command = new RegistrarAsistenciaCommand(EmpleadoId.of(empleadoUuid),
                ProyectoId.from(proyectoUuid), fecha, LocalDateTime.of(2025, 4, 1, 8, 0),
                LocalDateTime.of(2025, 4, 1, 17, 0), null);

        assertNotNull(useCase.registrarAsistencia(command));
        verify(asistenciaRepositoryPort).save(any(AsistenciaRegistro.class));
    }

    @Test
    @DisplayName("empleado inactivo → InactiveWorkerException")
    void empleadoInactivo_lanzaInactiveWorkerException() {
        Empleado empleado = empleadoActivo();
        empleado.inactivar(LocalDate.of(2025, 1, 1));
        when(empleadoRepositoryPort.findById(EmpleadoId.of(empleadoUuid))).thenReturn(Optional.of(empleado));

        RegistrarAsistenciaCommand command = comandoValido();

        assertThrows(InactiveWorkerException.class, () -> useCase.registrarAsistencia(command));
    }

    @Test
    @DisplayName("proyecto no ACTIVO → ProyectoNoActivoException")
    void proyectoSuspendido_lanzaProyectoNoActivoException() {
        when(empleadoRepositoryPort.findById(EmpleadoId.of(empleadoUuid))).thenReturn(Optional.of(empleadoActivo()));
        when(proyectoRepositoryPort.findById(ProyectoId.from(proyectoUuid)))
                .thenReturn(Optional.of(Proyecto.reconstruir(ProyectoId.from(proyectoUuid), "Obra", "Lima",
                        EstadoProyecto.SUSPENDIDO)));

        assertThrows(ProyectoNoActivoException.class, () -> useCase.registrarAsistencia(comandoValido()));
    }

    @Test
    @DisplayName("sin asignación vigente al proyecto → TrabajadorNoAsignadoAlProyectoException")
    void sinAsignacion_lanzaTrabajadorNoAsignadoAlProyectoException() {
        LocalDate fecha = LocalDate.of(2025, 4, 1);
        when(empleadoRepositoryPort.findById(EmpleadoId.of(empleadoUuid))).thenReturn(Optional.of(empleadoActivo()));
        when(proyectoRepositoryPort.findById(ProyectoId.from(proyectoUuid)))
                .thenReturn(Optional.of(Proyecto.reconstruir(ProyectoId.from(proyectoUuid), "Obra", "Lima",
                        EstadoProyecto.ACTIVO)));
        when(asignacionProyectoRepositoryPort.existsVigenteAsignacionEmpleadoProyectoEnFecha(
                EmpleadoId.of(empleadoUuid), ProyectoId.from(proyectoUuid), fecha)).thenReturn(false);

        assertThrows(TrabajadorNoAsignadoAlProyectoException.class, () -> useCase.registrarAsistencia(comandoValido()));
    }

    @Test
    @DisplayName("marcas incoherentes con fecha de tareo → IllegalArgumentException")
    void coherenciaTemporalInvalida_lanzaIae() {
        when(empleadoRepositoryPort.findById(EmpleadoId.of(empleadoUuid))).thenReturn(Optional.of(empleadoActivo()));
        when(proyectoRepositoryPort.findById(ProyectoId.from(proyectoUuid)))
                .thenReturn(Optional.of(Proyecto.reconstruir(ProyectoId.from(proyectoUuid), "Obra", "Lima",
                        EstadoProyecto.ACTIVO)));

        LocalDate fecha = LocalDate.of(2025, 4, 1);
        RegistrarAsistenciaCommand command = new RegistrarAsistenciaCommand(EmpleadoId.of(empleadoUuid),
                ProyectoId.from(proyectoUuid), fecha, LocalDateTime.of(2025, 4, 2, 8, 0),
                LocalDateTime.of(2025, 4, 2, 17, 0), null);

        assertThrows(IllegalArgumentException.class, () -> useCase.registrarAsistencia(command));
    }

    @Test
    @DisplayName("solape con registros existentes → AsistenciaSuperpuestaException")
    void solape_lanzaAsistenciaSuperpuestaException() {
        LocalDate fecha = LocalDate.of(2025, 4, 1);
        when(empleadoRepositoryPort.findById(EmpleadoId.of(empleadoUuid))).thenReturn(Optional.of(empleadoActivo()));
        when(proyectoRepositoryPort.findById(ProyectoId.from(proyectoUuid)))
                .thenReturn(Optional.of(Proyecto.reconstruir(ProyectoId.from(proyectoUuid), "Obra", "Lima",
                        EstadoProyecto.ACTIVO)));
        when(asignacionProyectoRepositoryPort.existsVigenteAsignacionEmpleadoProyectoEnFecha(
                eq(EmpleadoId.of(empleadoUuid)), eq(ProyectoId.from(proyectoUuid)), eq(fecha))).thenReturn(true);
        AsistenciaRegistro existente = AsistenciaRegistro.registrar(AsistenciaId.random(), EmpleadoId.of(empleadoUuid),
                ProyectoId.from(proyectoUuid), fecha, java.time.LocalTime.of(9, 0), java.time.LocalTime.of(10, 0),
                null);
        when(asistenciaRepositoryPort.findOverlapping(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(existente));

        assertThrows(AsistenciaSuperpuestaException.class, () -> useCase.registrarAsistencia(comandoValido()));
    }

    private RegistrarAsistenciaCommand comandoValido() {
        LocalDate fecha = LocalDate.of(2025, 4, 1);
        return new RegistrarAsistenciaCommand(EmpleadoId.of(empleadoUuid), ProyectoId.from(proyectoUuid), fecha,
                LocalDateTime.of(2025, 4, 1, 8, 0), LocalDateTime.of(2025, 4, 1, 17, 0), null);
    }

    private static Empleado empleadoActivo() {
        Contacto contacto = Contacto.of("e@test.com", null, null);
        return Empleado.crear(EmpleadoId.of(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")), "Juan",
                "Pérez", "DOC-1", contacto, LocalDate.of(2024, 1, 1), new BigDecimal("3000"), "Oficial",
                TipoEmpleado.PERMANENTE);
    }
}
