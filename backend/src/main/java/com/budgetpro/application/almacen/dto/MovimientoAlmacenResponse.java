package com.budgetpro.application.almacen.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de respuesta para un movimiento de almacén.
 */
public record MovimientoAlmacenResponse(
    UUID id,
    UUID almacenId,
    UUID recursoId,
    String tipoMovimiento,
    LocalDate fechaMovimiento,
    BigDecimal cantidad,
    BigDecimal precioUnitario,
    BigDecimal importeTotal,
    String numeroDocumento,
    UUID partidaId,
    UUID centroCostoId,
    String observaciones,
    BigDecimal stockActual,
    BigDecimal costoPromedioPonderado
) {}
