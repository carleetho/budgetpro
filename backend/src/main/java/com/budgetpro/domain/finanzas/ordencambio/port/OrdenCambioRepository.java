package com.budgetpro.domain.finanzas.ordencambio.port;

import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambio;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambioId;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de repositorio para operaciones CRUD básicas de OrdenCambio.
 */
public interface OrdenCambioRepository {

    /**
     * Guarda una orden de cambio (creación o actualización).
     *
     * @param ordenCambio La entidad a guardar
     * @return La entidad guardada
     */
    OrdenCambio save(OrdenCambio ordenCambio);

    /**
     * Busca una orden de cambio por su ID.
     *
     * @param id El ID de la orden
     * @return Un Optional conteniendo la orden si existe
     */
    Optional<OrdenCambio> findById(OrdenCambioId id);

    /**
     * Elimina una orden de cambio por su ID.
     *
     * @param id El ID de la orden a eliminar
     */
    void delete(OrdenCambioId id);

    /**
     * Verifica si existe una orden con el número dado en un proyecto. Útil para
     * validar unicidad.
     *
     * @param proyectoId  ID del proyecto
     * @param numeroOrden Número de la orden (ej: "OC-001")
     * @return true si ya existe una orden con ese número en el proyecto
     */
    boolean existsByNumeroOrden(UUID proyectoId, String numeroOrden);
}
