package com.budgetpro.domain.rrhh.model;

import com.budgetpro.domain.proyecto.model.ProyectoId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AsistenciaRegistroTest {

    @Test
    @DisplayName("registrar: entrada y salida idénticas → IllegalArgumentException")
    void registrar_entradaIgualSalida_lanzaIae() {
        assertThrows(IllegalArgumentException.class,
                () -> AsistenciaRegistro.registrar(AsistenciaId.random(), EmpleadoId.of(UUID.randomUUID()),
                        ProyectoId.from(UUID.randomUUID()), LocalDate.of(2025, 4, 1), LocalTime.of(8, 0),
                        LocalTime.of(8, 0), null));
    }
}
