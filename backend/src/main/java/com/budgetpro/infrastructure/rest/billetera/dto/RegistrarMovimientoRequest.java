package com.budgetpro.infrastructure.rest.billetera.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record RegistrarMovimientoRequest(
        @NotNull(message = "El monto es obligatorio") @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero") BigDecimal monto,

        @NotBlank(message = "La moneda es obligatoria") @Pattern(regexp = "PEN|USD|EUR|pen|usd|eur", message = "La moneda debe ser PEN, USD o EUR") String moneda,

        @NotBlank(message = "El tipo es obligatorio") @Pattern(regexp = "INGRESO|EGRESO", message = "El tipo debe ser INGRESO o EGRESO") String tipo,

        @NotBlank(message = "La referencia es obligatoria") String referencia,

        String evidenciaUrl) {
}
