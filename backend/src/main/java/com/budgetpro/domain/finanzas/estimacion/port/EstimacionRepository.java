package com.budgetpro.domain.finanzas.estimacion.port;

import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EstimacionRepository {

    Estimacion save(Estimacion estimacion);

    Optional<Estimacion> findById(EstimacionId id);

    List<Estimacion> findByProyectoId(UUID proyectoId);

    List<Estimacion> findByProyectoIdAndEstado(UUID proyectoId, EstadoEstimacion estado);

    void delete(EstimacionId id);

    boolean existsPeriodoSolapado(UUID proyectoId, LocalDate inicio, LocalDate fin);

    /**
     * Verifica si existe alguna estimación solapada excluyendo una específica (útil
     * para actualizaciones).
     */
    boolean existsPeriodoSolapadoExcludingId(UUID proyectoId, LocalDate inicio, LocalDate fin, EstimacionId excludeId);

    /**
     * Obtiene el siguiente número de estimación para un proyecto.
     */
    Integer obtenerSiguienteNumeroEstimacion(UUID proyectoId);
}
