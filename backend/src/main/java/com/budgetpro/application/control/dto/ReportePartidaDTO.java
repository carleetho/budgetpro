package com.budgetpro.application.control.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO que representa una partida en el reporte de control de costos.
 * 
 * Incluye datos del PLAN (presupuesto) y REAL (ejecutado) con desviaciones.
 */
public record ReportePartidaDTO(
        UUID id,
        String item,
        String descripcion,
        String unidad,
        Integer nivel,
        
        // PLAN (Meta)
        BigDecimal metrado,
        BigDecimal precioUnitario,
        BigDecimal parcialPlan, // Metrado * Precio Unitario
        
        // REAL (Ejecutado)
        BigDecimal gastoAcumulado, // Suma de consumos
        
        // DESVIACIÓN
        BigDecimal saldo, // ParcialPlan - GastoAcumulado
        BigDecimal porcentajeEjecucion, // (GastoAcumulado / ParcialPlan) * 100
        
        // Jerarquía
        UUID padreId,
        List<ReportePartidaDTO> hijos
) {
}
