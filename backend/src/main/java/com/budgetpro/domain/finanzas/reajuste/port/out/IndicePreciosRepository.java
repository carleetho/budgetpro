package com.budgetpro.domain.finanzas.reajuste.port.out;

import com.budgetpro.domain.finanzas.reajuste.model.IndicePrecios;
import com.budgetpro.domain.finanzas.reajuste.model.IndicePreciosId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de índices de precios.
 */
public interface IndicePreciosRepository {
    
    /**
     * Guarda un índice de precios.
     */
    void guardar(IndicePrecios indice);
    
    /**
     * Busca un índice por ID.
     */
    Optional<IndicePrecios> buscarPorId(IndicePreciosId id);
    
    /**
     * Busca un índice por código y fecha base.
     */
    Optional<IndicePrecios> buscarPorCodigoYFecha(String codigo, LocalDate fechaBase);
    
    /**
     * Busca el índice más cercano a una fecha para un código dado.
     * Si existe un índice para la fecha exacta, lo retorna.
     * Si no, busca el índice más reciente anterior a la fecha.
     */
    Optional<IndicePrecios> buscarIndiceMasCercano(String codigo, LocalDate fecha);
    
    /**
     * Busca todos los índices activos de un código dado.
     */
    List<IndicePrecios> buscarActivosPorCodigo(String codigo);
}
