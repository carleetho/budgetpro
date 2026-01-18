package com.budgetpro.infrastructure.rest.almacen.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de request para registrar un movimiento de almacén.
 */
public record RegistrarMovimientoAlmacenRequest(
    @NotNull(message = "El ID del almacén es obligatorio")
    UUID almacenId,
    
    @NotNull(message = "El ID del recurso es obligatorio")
    UUID recursoId,
    
    @NotBlank(message = "El tipo de movimiento es obligatorio")
    String tipoMovimiento, // "ENTRADA" o "SALIDA"
    
    @NotNull(message = "La fecha del movimiento es obligatoria")
    LocalDate fechaMovimiento,
    
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.000001", message = "La cantidad debe ser mayor a cero")
    BigDecimal cantidad,
    
    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio unitario debe ser mayor o igual a cero")
    BigDecimal precioUnitario,
    
    String numeroDocumento,
    
    UUID partidaId, // Obligatorio para SALIDA
    
    UUID centroCostoId, // Opcional
    
    String observaciones
) {}
