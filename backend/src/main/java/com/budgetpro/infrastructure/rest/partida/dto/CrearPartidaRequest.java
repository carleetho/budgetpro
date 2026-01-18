package com.budgetpro.infrastructure.rest.partida.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de request REST para crear una partida.
 */
public record CrearPartidaRequest(
        @NotNull(message = "El ID del presupuesto es obligatorio")
        UUID presupuestoId,
        
        UUID padreId, // Opcional, para jerarquía recursiva
        
        @NotBlank(message = "El item (código WBS) es obligatorio")
        String item,
        
        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,
        
        String unidad, // Opcional si es título
        
        @DecimalMin(value = "0.0", message = "El metrado no puede ser negativo")
        BigDecimal metrado, // Cantidad presupuestada. 0 si es título
        
        @NotNull(message = "El nivel es obligatorio")
        Integer nivel // Profundidad en el árbol: 1, 2, 3...
) {
}
