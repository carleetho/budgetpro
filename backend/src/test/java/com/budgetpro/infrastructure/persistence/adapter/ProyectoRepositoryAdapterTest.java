package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.finanzas.proyecto.model.FrecuenciaControl;
import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests de integración para ProyectoRepositoryAdapter.
 */
@Transactional
class ProyectoRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Test
    @DisplayName("findAllWithFrecuenciaControl retorna solo proyectos con frecuenciaControl no nulo")
    void findAllWithFrecuenciaControl_retornaSoloProyectosConFrecuencia() {
        ProyectoId p1Id = ProyectoId.nuevo();
        ProyectoId p2Id = ProyectoId.nuevo();
        ProyectoId p3Id = ProyectoId.nuevo();

        Proyecto p1 = Proyecto.reconstruir(
                p1Id, "P1 Frecuencia MENSUAL", "Loc", EstadoProyecto.ACTIVO, null, FrecuenciaControl.MENSUAL);
        Proyecto p2 = Proyecto.crear(p2Id, "P2 Sin Frecuencia", "Loc");
        Proyecto p3 = Proyecto.reconstruir(
                p3Id, "P3 Frecuencia SEMANAL", "Loc", EstadoProyecto.ACTIVO, null, FrecuenciaControl.SEMANAL);

        proyectoRepository.save(p1);
        proyectoRepository.save(p2);
        proyectoRepository.save(p3);

        List<Proyecto> result = proyectoRepository.findAllWithFrecuenciaControl();

        assertThat(result).anyMatch(pr -> pr.getId().equals(p1Id) && pr.getFrecuenciaControl() == FrecuenciaControl.MENSUAL);
        assertThat(result).anyMatch(pr -> pr.getId().equals(p3Id) && pr.getFrecuenciaControl() == FrecuenciaControl.SEMANAL);
        assertThat(result).noneMatch(pr -> pr.getId().equals(p2Id));
    }

    @Test
    @DisplayName("Round-trip: guardar Proyecto con SEMANAL y recuperar con frecuenciaControl correcto")
    void roundTrip_frecuenciaControlPersisteCorrectamente() {
        ProyectoId id = ProyectoId.nuevo();
        Proyecto original = Proyecto.reconstruir(
                id, "Proyecto SEMANAL", "Loc", EstadoProyecto.ACTIVO, null, FrecuenciaControl.SEMANAL);

        proyectoRepository.save(original);

        Optional<Proyecto> recuperado = proyectoRepository.findById(id);
        assertTrue(recuperado.isPresent());
        assertEquals(FrecuenciaControl.SEMANAL, recuperado.get().getFrecuenciaControl());
    }

    @Test
    @DisplayName("Round-trip: fechaInicio y frecuenciaControl persisten; esFechaCorteValida funciona tras reload")
    void roundTrip_fechaInicioYFrecuenciaPersisten() {
        ProyectoId id = ProyectoId.nuevo();
        LocalDateTime fechaInicio = LocalDateTime.of(2025, 1, 6, 0, 0);
        Proyecto original = Proyecto.reconstruir(
                id, "Proyecto Con Fechas", "Loc", EstadoProyecto.ACTIVO, fechaInicio, FrecuenciaControl.SEMANAL);

        proyectoRepository.save(original);

        Proyecto recuperado = proyectoRepository.findById(id).orElseThrow();
        assertEquals(fechaInicio, recuperado.getFechaInicio());
        assertEquals(FrecuenciaControl.SEMANAL, recuperado.getFrecuenciaControl());
        assertTrue(recuperado.esFechaCorteValida(LocalDate.of(2025, 1, 13)));
    }
}
