package com.budgetpro.application.explosion.port.in;

import com.budgetpro.application.explosion.dto.ExplosionInsumosResponse;

import java.util.UUID;

/**
 * Puerto de entrada para el caso de uso de explosión de insumos de un presupuesto.
 */
public interface ExplotarInsumosPresupuestoUseCase {

    /**
     * Explota los insumos de un presupuesto, agregando cantidades totales normalizadas por unidad base.
     * 
     * Solo considera partidas hoja (sin hijos en WBS) y agrupa recursos por tipo.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return La explosión de insumos agrupada por tipo de recurso
     * @throws com.budgetpro.application.presupuesto.exception.PresupuestoNoEncontradoException si el presupuesto no existe
     */
    ExplosionInsumosResponse ejecutar(UUID presupuestoId);
}
