package com.budgetpro.domain.proyecto.model;

import com.budgetpro.domain.finanzas.proyecto.model.FrecuenciaControl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProyectoFrecuenciaTest {

    @Test
    @DisplayName("SEMANAL + fechaInicio 2025-01-06: esFechaCorteValida(2025-01-13) -> true")
    void semanalFechaCorteValidaSieteDias() {
        Proyecto proyecto = Proyecto.reconstruir(
                ProyectoId.nuevo(),
                "Proyecto SEMANAL",
                "Ubicación",
                EstadoProyecto.ACTIVO,
                LocalDateTime.of(2025, 1, 6, 0, 0),
                FrecuenciaControl.SEMANAL
        );

        assertTrue(proyecto.esFechaCorteValida(LocalDate.of(2025, 1, 13)));
    }

    @Test
    @DisplayName("SEMANAL + fechaInicio 2025-01-06: esFechaCorteValida(2025-01-10) -> false")
    void semanalFechaCorteInvalida() {
        Proyecto proyecto = Proyecto.reconstruir(
                ProyectoId.nuevo(),
                "Proyecto SEMANAL",
                "Ubicación",
                EstadoProyecto.ACTIVO,
                LocalDateTime.of(2025, 1, 6, 0, 0),
                FrecuenciaControl.SEMANAL
        );

        assertFalse(proyecto.esFechaCorteValida(LocalDate.of(2025, 1, 10)));
    }

    @Test
    @DisplayName("frecuenciaControl=null: esFechaCorteValida(any date) -> true")
    void sinFrecuenciaCualquierFechaValida() {
        Proyecto proyecto = Proyecto.crear(ProyectoId.nuevo(), "Proyecto Sin Frecuencia", "Ubicación");

        assertTrue(proyecto.esFechaCorteValida(LocalDate.of(2025, 1, 13)));
        assertTrue(proyecto.esFechaCorteValida(LocalDate.of(2025, 6, 15)));
    }
}
