package com.budgetpro.application.sobrecosto.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de comando para configurar el análisis de sobrecosto de un presupuesto.
 */
public record ConfigurarSobrecostoCommand(
        UUID presupuestoId,
        
        // Indirectos
        BigDecimal porcentajeIndirectosOficinaCentral,
        BigDecimal porcentajeIndirectosOficinaCampo,
        
        // Financiamiento
        BigDecimal porcentajeFinanciamiento,
        Boolean financiamientoCalculado,
        
        // Utilidad
        BigDecimal porcentajeUtilidad,
        
        // Cargos Adicionales
        BigDecimal porcentajeFianzas,
        BigDecimal porcentajeImpuestosReflejables
) {
}
