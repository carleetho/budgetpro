package com.budgetpro.application.explosion.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO de respuesta para la explosión de insumos de un presupuesto.
 * 
 * Agrupa los recursos por tipo (MATERIAL, MANO_OBRA, EQUIPO_MAQUINA, etc.)
 */
public record ExplosionInsumosResponse(
        Map<String, List<RecursoAgregadoDTO>> recursosPorTipo
) {
}
