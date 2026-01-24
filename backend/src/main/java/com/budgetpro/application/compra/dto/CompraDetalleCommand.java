package com.budgetpro.application.compra.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de comando para un detalle de compra.
 */
public record CompraDetalleCommand(
        @NotNull(message = "El ID externo del recurso es obligatorio")
        String recursoExternalId,

        @NotNull(message = "El nombre del recurso es obligatorio")
        String recursoNombre,

        /** Unidad en que llega la compra (Authority by PO). Si null, se usa la unidad del catálogo. */
        String unidad,

        UUID partidaId,

        @NotNull(message = "La naturaleza del gasto es obligatoria")
        com.budgetpro.domain.logistica.compra.model.NaturalezaGasto naturalezaGasto,

        @NotNull(message = "La relación contractual es obligatoria")
        com.budgetpro.domain.logistica.compra.model.RelacionContractual relacionContractual,

        @NotNull(message = "El rubro es obligatorio")
        com.budgetpro.domain.logistica.compra.model.RubroInsumo rubroInsumo,
        
        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.0", message = "La cantidad no puede ser negativa")
        BigDecimal cantidad,
        
        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")
        BigDecimal precioUnitario
) {
}
