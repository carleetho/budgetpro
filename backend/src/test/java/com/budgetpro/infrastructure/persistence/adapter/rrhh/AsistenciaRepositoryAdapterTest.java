package com.budgetpro.infrastructure.persistence.adapter.rrhh;

import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.EstadoAsistencia;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.AsistenciaRegistroEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.EmpleadoEntity;
import com.budgetpro.infrastructure.persistence.mapper.rrhh.AsistenciaMapper;
import com.budgetpro.infrastructure.persistence.repository.rrhh.AsistenciaRegistroJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsistenciaRepositoryAdapterTest {

    @Mock
    private AsistenciaRegistroJpaRepository jpaRepository;

    private AsistenciaRepositoryAdapter adapter;

    private final UUID empleadoUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID proyectoP1 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID proyectoP2 = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @BeforeEach
    void setUp() {
        adapter = new AsistenciaRepositoryAdapter(jpaRepository, new AsistenciaMapper());
    }

    @Test
    void findOverlapping_mismoDiaVentanasDisjuntas_noDevuelveCandidatos() {
        EmpleadoId empleadoId = EmpleadoId.of(empleadoUuid);
        ProyectoId nuevoProyecto = ProyectoId.from(proyectoP2);
        LocalDate fecha = LocalDate.of(2025, 3, 15);

        AsistenciaRegistroEntity existente = entity(empleadoUuid, proyectoP1, fecha, LocalTime.of(8, 0), LocalTime.of(16, 0));
        when(jpaRepository.findByEmpleadoIdAndFechaBetween(eq(empleadoUuid), eq(fecha.minusDays(1)), eq(fecha.plusDays(1))))
                .thenReturn(List.of(existente));

        LocalDateTime inicio = LocalDateTime.of(fecha, LocalTime.of(17, 0));
        LocalDateTime fin = LocalDateTime.of(fecha, LocalTime.of(22, 0));

        assertTrue(adapter.findOverlapping(empleadoId, nuevoProyecto, inicio, fin).isEmpty());
    }

    @Test
    void findOverlapping_solapamientoParcial_devuelveElRegistroQueColisiona() {
        EmpleadoId empleadoId = EmpleadoId.of(empleadoUuid);
        ProyectoId proyectoId = ProyectoId.from(proyectoP1);
        LocalDate fecha = LocalDate.of(2025, 3, 15);

        AsistenciaRegistroEntity existente = entity(empleadoUuid, proyectoP1, fecha, LocalTime.of(8, 0), LocalTime.of(16, 0));
        when(jpaRepository.findByEmpleadoIdAndFechaBetween(eq(empleadoUuid), eq(fecha.minusDays(1)), eq(fecha.plusDays(1))))
                .thenReturn(List.of(existente));

        LocalDateTime inicio = LocalDateTime.of(fecha, LocalTime.of(15, 0));
        LocalDateTime fin = LocalDateTime.of(fecha, LocalTime.of(20, 0));

        assertEquals(1, adapter.findOverlapping(empleadoId, proyectoId, inicio, fin).size());
    }

    @Test
    void findByProyectoAndPeriodo_delegaAJpaYMapeaDominio() {
        ProyectoId proyectoId = ProyectoId.from(proyectoP1);
        LocalDate inicio = LocalDate.of(2025, 4, 1);
        LocalDate fin = LocalDate.of(2025, 4, 30);
        AsistenciaRegistroEntity e1 = entity(empleadoUuid, proyectoP1, LocalDate.of(2025, 4, 10), LocalTime.of(8, 0),
                LocalTime.of(17, 0));
        when(jpaRepository.findByProyectoIdAndFechaBetween(eq(proyectoP1), eq(inicio), eq(fin))).thenReturn(List.of(e1));

        var resultado = adapter.findByProyectoAndPeriodo(proyectoId, inicio, fin);

        assertEquals(1, resultado.size());
        assertEquals(ProyectoId.from(proyectoP1), resultado.get(0).getProyectoId());
        assertEquals(LocalDate.of(2025, 4, 10), resultado.get(0).getFecha());
        verify(jpaRepository).findByProyectoIdAndFechaBetween(proyectoP1, inicio, fin);
    }

    @Test
    void findOverlapping_turnoNocturnoExistenteYTareoDiaSiguiente_solapa() {
        EmpleadoId empleadoId = EmpleadoId.of(empleadoUuid);
        ProyectoId proyectoId = ProyectoId.from(proyectoP1);
        LocalDate diaTurnoNoche = LocalDate.of(2025, 3, 15);
        LocalDate diaNuevo = LocalDate.of(2025, 3, 16);

        AsistenciaRegistroEntity existente = entity(empleadoUuid, proyectoP1, diaTurnoNoche, LocalTime.of(22, 0),
                LocalTime.of(6, 0));
        when(jpaRepository.findByEmpleadoIdAndFechaBetween(eq(empleadoUuid), eq(diaNuevo.minusDays(1)),
                eq(diaNuevo.plusDays(1)))).thenReturn(List.of(existente));

        LocalDateTime inicio = LocalDateTime.of(diaNuevo, LocalTime.of(4, 0));
        LocalDateTime fin = LocalDateTime.of(diaNuevo, LocalTime.of(12, 0));

        assertEquals(1, adapter.findOverlapping(empleadoId, proyectoId, inicio, fin).size());
    }

    private static AsistenciaRegistroEntity entity(UUID empleadoId, UUID proyectoId, LocalDate fecha, LocalTime entrada,
            LocalTime salida) {
        AsistenciaRegistroEntity e = new AsistenciaRegistroEntity();
        e.setId(UUID.randomUUID());
        EmpleadoEntity em = new EmpleadoEntity();
        em.setId(empleadoId);
        e.setEmpleado(em);
        ProyectoEntity p = new ProyectoEntity();
        p.setId(proyectoId);
        e.setProyecto(p);
        e.setFecha(fecha);
        e.setHoraEntrada(entrada);
        e.setHoraSalida(salida);
        e.setEstado(EstadoAsistencia.PRESENTE);
        return e;
    }
}
