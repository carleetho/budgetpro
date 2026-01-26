package com.budgetpro.domain.finanzas.avance.port.out;

import com.budgetpro.domain.finanzas.avance.model.Valuacion;
import com.budgetpro.domain.finanzas.avance.model.ValuacionId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado Valuacion.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface ValuacionRepository {

    /**
     * Guarda una valuación.
     * 
     * @param valuacion La valuación a guardar
     */
    void save(Valuacion valuacion);

    /**
     * Busca una valuación por su ID.
     * 
     * @param id El ID de la valuación
     * @return Optional con la valuación si existe, vacío en caso contrario
     */
    Optional<Valuacion> findById(ValuacionId id);

    /**
     * Busca todas las valuaciones de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de valuaciones del proyecto
     */
    List<Valuacion> findByProyectoId(UUID proyectoId);
}
