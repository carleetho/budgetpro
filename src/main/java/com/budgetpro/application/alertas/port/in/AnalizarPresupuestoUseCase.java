package com.budgetpro.application.alertas.port.in;

import com.budgetpro.application.alertas.dto.AnalisisPresupuestoResponse;

import java.util.UUID;

/**
 * Caso de uso para analizar un presupuesto y generar alertas paramétricas.
 */
public interface AnalizarPresupuestoUseCase {
    
    /**
     * Analiza un presupuesto y genera alertas paramétricas.
     * 
     * @param presupuestoId ID del presupuesto a analizar
     * @return AnalisisPresupuestoResponse con las alertas generadas
     */
    AnalisisPresupuestoResponse analizar(UUID presupuestoId);
}
