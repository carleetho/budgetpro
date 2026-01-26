package com.budgetpro.domain.logistica.almacen.port.out;

import com.budgetpro.domain.logistica.almacen.model.Almacen;
import com.budgetpro.domain.logistica.almacen.model.AlmacenId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia de almacenes.
 */
public interface AlmacenRepository {
    
    /**
     * Guarda un almacén.
     */
    void guardar(Almacen almacen);
    
    /**
     * Busca un almacén por ID.
     */
    Optional<Almacen> buscarPorId(AlmacenId id);
    
    /**
     * Busca todos los almacenes activos de un proyecto.
     */
    List<Almacen> buscarActivosPorProyectoId(UUID proyectoId);
}
