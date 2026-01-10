package com.budgetpro.application.finanzas.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa el comando para crear una nueva billetera.
 * 
 * Usado como entrada del caso de uso CrearBilleteraUseCase.
 * 
 * REGLA: Cada proyecto tiene UNA sola billetera (relación 1:1).
 */
public record CrearBilleteraCommand(
        @NotNull(message = "El ID del proyecto es obligatorio")
        UUID proyectoId
) {
}
