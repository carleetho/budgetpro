package com.budgetpro.application.rrhh.dto;

import java.util.UUID;

public record ActualizarCuadrillaCommand(UUID cuadrillaId, UUID nuevoLiderId, UUID agregarMiembroId,
        UUID removerMiembroId, Boolean inactivar) {
}
