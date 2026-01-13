package com.budgetpro.application.control.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para el reporte de control de costos.
 * 
 * Contiene el resumen total y la lista jerárquica de partidas.
 */
public record ReporteControlCostosResponse(
        UUID presupuestoId,
        String nombrePresupuesto,
        
        // Totales
        BigDecimal totalPlan,
        BigDecimal totalReal,
        BigDecimal totalSaldo,
        BigDecimal porcentajeEjecucionTotal,
        
        // Partidas (jerárquico)
        List<ReportePartidaDTO> partidas
) {
}
