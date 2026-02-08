package com.budgetpro.domain.finanzas.estimacion.port.out;

import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado
 * Estimacion.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface EstimacionRepository {

    /**
     * Guarda una estimación.
     * 
     * @param estimacion La estimación a guardar
     */
    void save(Estimacion estimacion);

    /**
     * Busca una estimación por su ID.
     * 
     * @param id El ID de la estimación
     * @return Optional con la estimación si existe, vacío en caso contrario
     */
    Optional<Estimacion> findById(EstimacionId id);

    /**
     * Busca todas las estimaciones de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de estimaciones del proyecto
     */
    List<Estimacion> findByProyectoId(UUID proyectoId);

    /**
     * Busca la siguiente estimación (para calcular el número consecutivo).
     * 
     * @param proyectoId El ID del proyecto
     * @return El número de la última estimación + 1, o 1 si no hay estimaciones
     *         previas
     */
    Integer obtenerSiguienteNumeroEstimacion(UUID proyectoId);

    /**
     * Busca todas las estimaciones aprobadas de un proyecto (para calcular
     * acumulados).
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de estimaciones aprobadas
     */
    List<Estimacion> findAprobadasByProyectoId(UUID proyectoId);

    /**
     * Busca una estimación por proyecto y número de estimación. Usado para validar
     * la regla de aprobación secuencial (ES-01/REGLA-010).
     * 
     * @param proyectoId       El ID del proyecto
     * @param numeroEstimacion El número de la estimación (consecutivo: 1, 2, 3...)
     * @return Optional con la estimación si existe, vacío en caso contrario
     */
    Optional<Estimacion> findByProyectoIdAndNumero(UUID proyectoId, Integer numeroEstimacion);
}
