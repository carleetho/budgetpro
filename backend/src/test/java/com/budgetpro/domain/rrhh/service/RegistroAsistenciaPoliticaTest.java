package com.budgetpro.domain.rrhh.service;

import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.exception.InactiveWorkerException;
import com.budgetpro.domain.rrhh.exception.ProyectoNoActivoParaOperacionException;
import com.budgetpro.domain.rrhh.exception.SolapeHorarioTareoException;
import com.budgetpro.domain.rrhh.exception.TrabajadorNoAsignadoAlProyectoException;
import com.budgetpro.domain.rrhh.model.AsignacionProyectoId;
import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.AsistenciaId;
import com.budgetpro.domain.rrhh.model.AsistenciaRegistro;
import com.budgetpro.domain.rrhh.model.Contacto;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;
import com.budgetpro.domain.rrhh.model.TipoEmpleado;
import com.budgetpro.domain.catalogo.model.RecursoProxyId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistroAsistenciaPoliticaTest {

    private static final UUID EMP_UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID PRJ_UUID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    @DisplayName("empleado inactivo → InactiveWorkerException (R-02)")
    void empleadoInactivo_lanzaInactiveWorkerException() {
        Empleado empleado = empleadoEnEstado(EstadoEmpleado.INACTIVO);
        assertThrows(InactiveWorkerException.class, () -> RegistroAsistenciaPolitica.validarEmpleadoActivoParaTareo(empleado));
    }

    @Test
    @DisplayName("empleado activo → sin excepción")
    void empleadoActivo_ok() {
        Empleado empleado = empleadoEnEstado(EstadoEmpleado.ACTIVO);
        assertDoesNotThrow(() -> RegistroAsistenciaPolitica.validarEmpleadoActivoParaTareo(empleado));
    }

    @Test
    @DisplayName("proyecto no ACTIVO → ProyectoNoActivoParaOperacionException (REGLA-150)")
    void proyectoSuspendido_lanzaProyectoNoActivoParaOperacionException() {
        Proyecto proyecto = Proyecto.reconstruir(ProyectoId.from(PRJ_UUID), "Obra", "Lima", EstadoProyecto.SUSPENDIDO);
        ProyectoNoActivoParaOperacionException ex = assertThrows(ProyectoNoActivoParaOperacionException.class,
                () -> RegistroAsistenciaPolitica.validarProyectoActivoParaTareo(proyecto));
        assertEquals(EstadoProyecto.SUSPENDIDO, ex.getEstado());
    }

    @Test
    @DisplayName("coherencia: entrada en fecha distinta al tareo → IllegalArgumentException")
    void coherencia_entradaDistintaFecha_lanzaIae() {
        LocalDate fecha = LocalDate.of(2025, 4, 1);
        LocalDateTime entrada = LocalDateTime.of(2025, 4, 2, 8, 0);
        LocalDateTime salida = LocalDateTime.of(2025, 4, 2, 17, 0);
        assertThrows(IllegalArgumentException.class,
                () -> RegistroAsistenciaPolitica.validarCoherenciaTemporalTareo(fecha, entrada, salida));
    }

    @Test
    @DisplayName("coherencia: turno nocturno válido (salida día siguiente)")
    void coherencia_nocturno_ok() {
        LocalDate fecha = LocalDate.of(2025, 4, 1);
        LocalDateTime entrada = LocalDateTime.of(2025, 4, 1, 22, 0);
        LocalDateTime salida = LocalDateTime.of(2025, 4, 2, 6, 0);
        assertDoesNotThrow(() -> RegistroAsistenciaPolitica.validarCoherenciaTemporalTareo(fecha, entrada, salida));
    }

    @Test
    @DisplayName("coherencia: mismo día con salida no posterior → IllegalArgumentException")
    void coherencia_mismoDiaSalidaNoPosterior_lanzaIae() {
        LocalDate fecha = LocalDate.of(2025, 4, 1);
        LocalDateTime entrada = LocalDateTime.of(2025, 4, 1, 10, 0);
        LocalDateTime salida = LocalDateTime.of(2025, 4, 1, 8, 0);
        assertThrows(IllegalArgumentException.class,
                () -> RegistroAsistenciaPolitica.validarCoherenciaTemporalTareo(fecha, entrada, salida));
    }

    @Test
    @DisplayName("sin asignación vigente → TrabajadorNoAsignadoAlProyectoException (REGLA-125)")
    void sinAsignacion_lanzaTrabajadorNoAsignado() {
        EmpleadoId eid = EmpleadoId.of(EMP_UUID);
        ProyectoId pid = ProyectoId.from(PRJ_UUID);
        LocalDate fecha = LocalDate.of(2025, 4, 1);
        assertThrows(TrabajadorNoAsignadoAlProyectoException.class,
                () -> RegistroAsistenciaPolitica.validarAsignacionVigenteAlProyecto(eid, pid, fecha, false));
    }

    @Test
    @DisplayName("solapes detectados → SolapeHorarioTareoException (REGLA-125)")
    void solapes_lanzaSolapeHorarioTareoException() {
        AsistenciaRegistro otro = AsistenciaRegistro.registrar(AsistenciaId.random(), EmpleadoId.of(EMP_UUID),
                ProyectoId.from(PRJ_UUID), LocalDate.of(2025, 4, 1), java.time.LocalTime.of(7, 0),
                java.time.LocalTime.of(9, 0), null);
        assertThrows(SolapeHorarioTareoException.class,
                () -> RegistroAsistenciaPolitica.validarSinSolapeConRegistrosExistentes(List.of(otro)));
    }

    @Test
    @DisplayName("delegación R-03 con stub bloqueado → UnsupportedOperationException")
    void delegarR03_stubBloqueado_lanzaUoe() {
        EmpleadoId eid = EmpleadoId.of(EMP_UUID);
        LocalDate fecha = LocalDate.of(2025, 4, 1);
        AsignacionProyecto ap = AsignacionProyecto.crear(AsignacionProyectoId.generate(), eid,
                ProyectoId.from(PRJ_UUID), RecursoProxyId.generate(), fecha, null, null, null);
        assertThrows(UnsupportedOperationException.class,
                () -> RegistroAsistenciaPolitica.delegarValidacionSolapeAsignacionR03(
                        new AmbiguityBlockedAsignacionSolapeValidator(), eid, fecha, List.of(ap)));
    }

    @Test
    @DisplayName("delegación R-03 con NoOp → no lanza")
    void delegarR03_noOp_ok() {
        EmpleadoId eid = EmpleadoId.of(EMP_UUID);
        LocalDate fecha = LocalDate.of(2025, 4, 1);
        assertDoesNotThrow(() -> RegistroAsistenciaPolitica.delegarValidacionSolapeAsignacionR03(
                new NoOpAsignacionSolapeValidator(), eid, fecha, List.of()));
    }

    private static Empleado empleadoEnEstado(EstadoEmpleado estado) {
        Contacto c = Contacto.of("x@test.com", null, null);
        Empleado e = Empleado.crear(EmpleadoId.of(EMP_UUID), "Ana", "López", "D-1", c, LocalDate.of(2020, 1, 1),
                BigDecimal.TEN, "Oficial", TipoEmpleado.PERMANENTE);
        if (estado == EstadoEmpleado.INACTIVO) {
            e.inactivar(LocalDate.of(2025, 1, 1));
        }
        return e;
    }
}
