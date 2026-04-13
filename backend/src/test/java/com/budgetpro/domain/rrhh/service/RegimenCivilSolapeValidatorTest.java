package com.budgetpro.domain.rrhh.service;

import com.budgetpro.domain.catalogo.model.RecursoProxyId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.exception.AsignacionSuperpuestaException;
import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.AsignacionProyectoId;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegimenCivilSolapeValidatorTest {

    private final RegimenCivilSolapeValidator validator = new RegimenCivilSolapeValidator();

    private static final UUID E = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID P1 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID P2 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @Test
    @DisplayName("sin asignaciones → OK")
    void sinAsignaciones_ok() {
        EmpleadoId eid = EmpleadoId.of(E);
        assertDoesNotThrow(() -> validator.validar(eid, LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30), List.of()));
    }

    @Test
    @DisplayName("asignación ajena (otro empleado) → ignorada")
    void otroEmpleado_ignorado() {
        EmpleadoId eid = EmpleadoId.of(E);
        EmpleadoId otro = EmpleadoId.of(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"));
        AsignacionProyecto a = asignacion(otro, P1, LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30));
        assertDoesNotThrow(() -> validator.validar(eid, LocalDate.of(2025, 6, 15), LocalDate.of(2025, 6, 15), List.of(a)));
    }

    @Test
    @DisplayName("solape parcial de intervalos → AsignacionSuperpuestaException")
    void solapeParcial_lanza() {
        EmpleadoId eid = EmpleadoId.of(E);
        AsignacionProyecto existente = asignacion(eid, P1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30));
        assertThrows(AsignacionSuperpuestaException.class,
                () -> validator.validar(eid, LocalDate.of(2025, 6, 1), LocalDate.of(2025, 12, 31), List.of(existente)));
    }

    @Test
    @DisplayName("ventana abierta (fin null) intersecta asignación abierta → lanza")
    void ventanaAbiertaVsAsignacionAbierta_lanza() {
        EmpleadoId eid = EmpleadoId.of(E);
        AsignacionProyecto existente = asignacion(eid, P1, LocalDate.of(2025, 3, 1), null);
        assertThrows(AsignacionSuperpuestaException.class,
                () -> validator.validar(eid, LocalDate.of(2025, 12, 1), null, List.of(existente)));
    }

    @Test
    @DisplayName("adyacentes sin día común (fin = inicio del otro - 1 día) → OK")
    void sinSolapeContiguo_ok() {
        EmpleadoId eid = EmpleadoId.of(E);
        AsignacionProyecto existente = asignacion(eid, P1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        assertDoesNotThrow(() -> validator.validar(eid, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 6, 30), List.of(existente)));
    }

    @Test
    @DisplayName("intervalosSolapanInclusive: mismo día compartido → true")
    void helper_unDiaComun() {
        assertTrue(RegimenCivilSolapeValidator.intervalosSolapanInclusive(LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 10)));
    }

    @Test
    @DisplayName("intervalosSolapanInclusive: disjuntos → false")
    void helper_disjuntos() {
        assertFalse(RegimenCivilSolapeValidator.intervalosSolapanInclusive(LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 10)));
    }

    private static AsignacionProyecto asignacion(EmpleadoId empleadoId, UUID proyectoUuid, LocalDate ini, LocalDate fin) {
        return AsignacionProyecto.reconstruir(AsignacionProyectoId.generate(), empleadoId, ProyectoId.from(proyectoUuid),
                RecursoProxyId.generate(), ini, fin, null, null);
    }
}
