package com.budgetpro.domain.logistica.almacen.port.out;

import com.budgetpro.domain.logistica.almacen.model.MovimientoAlmacen;
import com.budgetpro.domain.logistica.almacen.model.MovimientoAlmacenId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia de movimientos de almacén.
 */
public interface MovimientoAlmacenRepository {
    
    /**
     * Guarda un movimiento de almacén.
     */
    void guardar(MovimientoAlmacen movimiento);
    
    /**
     * Busca un movimiento por ID.
     */
    Optional<MovimientoAlmacen> buscarPorId(MovimientoAlmacenId id);
    
    /**
     * Busca todos los movimientos de un almacén.
     */
    List<MovimientoAlmacen> buscarPorAlmacenId(UUID almacenId);
    
    /**
     * Busca todos los movimientos de un recurso en un almacén.
     */
    List<MovimientoAlmacen> buscarPorAlmacenIdYRecursoId(UUID almacenId, UUID recursoId);
}
