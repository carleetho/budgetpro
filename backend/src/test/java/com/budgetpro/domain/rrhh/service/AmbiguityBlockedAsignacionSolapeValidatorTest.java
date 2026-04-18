package com.budgetpro.domain.rrhh.service;

import com.budgetpro.domain.rrhh.model.AsignacionProyectoId;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.catalogo.model.RecursoProxyId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AmbiguityBlockedAsignacionSolapeValidatorTest {

    @Test
    @DisplayName("validar lanza UnsupportedOperationException con mensaje de bloqueo R-03 / PO")
    void validar_bloqueadoPorAmbiguedad_lanzaUnsupportedOperationException() {
        var validator = new AmbiguityBlockedAsignacionSolapeValidator();
        EmpleadoId empleadoId = EmpleadoId.of(java.util.UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        AsignacionProyecto existente = AsignacionProyecto.crear(AsignacionProyectoId.generate(), empleadoId,
                ProyectoId.from(java.util.UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")),
                RecursoProxyId.generate(),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30), null, null);

        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> validator.validar(empleadoId, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 12, 31),
                        List.of(existente)));

        assertEquals("Bloqueado por AMBIGUITY_DETECTED en R-03. Pendiente definición de PO", ex.getMessage());
    }
}
