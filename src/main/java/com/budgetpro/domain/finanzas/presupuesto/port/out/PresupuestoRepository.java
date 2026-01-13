package com.budgetpro.domain.finanzas.presupuesto.port.out;

import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado Presupuesto.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface PresupuestoRepository {

    /**
     * Guarda un presupuesto.
     * 
     * @param presupuesto El presupuesto a guardar
     */
    void save(Presupuesto presupuesto);

    /**
     * Busca un presupuesto por su ID.
     * 
     * @param id El ID del presupuesto
     * @return Optional con el presupuesto si existe, vacío en caso contrario
     */
    Optional<Presupuesto> findById(PresupuestoId id);

    /**
     * Busca el presupuesto activo de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Optional con el presupuesto si existe, vacío en caso contrario
     */
    Optional<Presupuesto> findByProyectoId(UUID proyectoId);

    /**
     * Verifica si existe un presupuesto activo para el proyecto dado.
     * 
     * @param proyectoId El ID del proyecto
     * @return true si existe, false en caso contrario
     */
    boolean existsByProyectoId(UUID proyectoId);
}
