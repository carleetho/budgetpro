package com.budgetpro.domain.logistica.bodega.port.out;

import com.budgetpro.domain.logistica.bodega.model.BodegaId;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para obtener la bodega por defecto de un proyecto.
 * Usado por InventarioSnapshotService para find-or-create (Authority by PO).
 */
public interface DefaultBodegaPort {

    /**
     * Obtiene la bodega por defecto del proyecto.
     * Típicamente la primera bodega activa ordenada por código.
     *
     * @param proyectoId ID del proyecto
     * @return Optional con BodegaId si existe, vacío en caso contrario
     */
    Optional<BodegaId> getDefaultForProject(UUID proyectoId);
}
