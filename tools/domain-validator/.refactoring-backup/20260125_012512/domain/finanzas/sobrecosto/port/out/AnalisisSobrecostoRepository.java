package com.budgetpro.domain.finanzas.sobrecosto.port.out;

import com.budgetpro.domain.finanzas.sobrecosto.model.AnalisisSobrecosto;
import com.budgetpro.domain.finanzas.sobrecosto.model.AnalisisSobrecostoId;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado AnalisisSobrecosto.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface AnalisisSobrecostoRepository {

    /**
     * Guarda un análisis de sobrecosto.
     * 
     * @param analisis El análisis a guardar
     */
    void save(AnalisisSobrecosto analisis);

    /**
     * Busca un análisis por su ID.
     * 
     * @param id El ID del análisis
     * @return Optional con el análisis si existe, vacío en caso contrario
     */
    Optional<AnalisisSobrecosto> findById(AnalisisSobrecostoId id);

    /**
     * Busca el análisis de sobrecosto de un presupuesto (relación 1:1).
     * 
     * @param presupuestoId El ID del presupuesto
     * @return Optional con el análisis si existe, vacío en caso contrario
     */
    Optional<AnalisisSobrecosto> findByPresupuestoId(UUID presupuestoId);
}
