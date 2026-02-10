package com.budgetpro.infrastructure.rest.reajuste.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de request para calcular reajuste de costos.
 */
public record CalcularReajusteRequest(
    @NotNull(message = "El ID del proyecto es obligatorio")
    UUID proyectoId,
    
    @NotNull(message = "El ID del presupuesto es obligatorio")
    UUID presupuestoId,
    
    @NotNull(message = "La fecha de corte es obligatoria")
    LocalDate fechaCorte,
    
    // REGLA-086
    @NotBlank(message = "El código del índice base es obligatorio")
    String indiceBaseCodigo,
    
    @NotNull(message = "La fecha del índice base es obligatoria")
    LocalDate indiceBaseFecha,
    
    @NotBlank(message = "El código del índice actual es obligatorio")
    String indiceActualCodigo,
    
    @NotNull(message = "La fecha del índice actual es obligatoria")
    LocalDate indiceActualFecha
) {}
