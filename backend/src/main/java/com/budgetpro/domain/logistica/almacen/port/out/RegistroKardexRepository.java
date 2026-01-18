package com.budgetpro.domain.logistica.almacen.port.out;

import com.budgetpro.domain.logistica.almacen.model.RegistroKardex;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia de registros de Kárdex.
 */
public interface RegistroKardexRepository {
    
    /**
     * Guarda un registro de Kárdex.
     */
    void guardar(RegistroKardex registro);
    
    /**
     * Busca el último registro de Kárdex para un almacén y recurso.
     */
    Optional<RegistroKardex> buscarUltimoPorAlmacenIdYRecursoId(UUID almacenId, UUID recursoId);
    
    /**
     * Busca todos los registros de Kárdex de un almacén y recurso ordenados por fecha.
     */
    List<RegistroKardex> buscarPorAlmacenIdYRecursoId(UUID almacenId, UUID recursoId);
}
