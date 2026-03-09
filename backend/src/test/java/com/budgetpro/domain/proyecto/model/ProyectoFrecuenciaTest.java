package com.budgetpro.domain.proyecto.model;

import com.budgetpro.domain.finanzas.proyecto.model.FrecuenciaControl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @DisplayName("SEMANAL + fechaInicio 2025-01-06: esFechaCorteValida(2025-01-06) same-day -> false")
    void semanalMismaFechaInvalida() {
        Proyecto proyecto = Proyecto.reconstruir(
                ProyectoId.nuevo(),
                "Proyecto SEMANAL",
                "Ubicación",
                EstadoProyecto.ACTIVO,
                LocalDateTime.of(2025, 1, 6, 0, 0),
                FrecuenciaControl.SEMANAL
        );

        assertFalse(proyecto.esFechaCorteValida(LocalDate.of(2025, 1, 6)));
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

    @Test
    @DisplayName("configurarFrecuencia con frecuencia no nula y fechaInicio nula lanza excepción")
    void configurarFrecuenciaSinFechaInicioLanza() {
        Proyecto proyecto = Proyecto.crear(ProyectoId.nuevo(), "Proyecto", "Loc");
        assertThrows(IllegalArgumentException.class,
                () -> proyecto.configurarFrecuencia(FrecuenciaControl.SEMANAL, null));
    }

    @Test
    @DisplayName("configurarFrecuencia retorna nueva instancia (patrón inmutable)")
    void configurarFrecuenciaRetornaNuevaInstancia() {
        Proyecto original =
                Proyecto.reconstruir(ProyectoId.nuevo(), "Proyecto", "Loc", EstadoProyecto.ACTIVO, null, null);
        LocalDateTime fechaInicio = LocalDateTime.of(2025, 1, 6, 0, 0);

        Proyecto conFrecuencia = original.configurarFrecuencia(FrecuenciaControl.SEMANAL, fechaInicio);

        assertTrue(original.getFrecuenciaControl() == null);
        assertTrue(conFrecuencia.getFrecuenciaControl() == FrecuenciaControl.SEMANAL);
        assertEquals(fechaInicio, conFrecuencia.getFechaInicio());
        assertNotSame(original, conFrecuencia);
    }
}
