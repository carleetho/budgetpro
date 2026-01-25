package com.budgetpro.infrastructure.rest.rrhh.dto;

import com.budgetpro.application.rrhh.dto.ActualizarCuadrillaCommand;

import java.util.UUID;

public record ActualizarMiembrosRequest(String nuevoLiderId, String agregarMiembroId, String removerMiembroId) {
    public ActualizarCuadrillaCommand toCommand(String cuadrillaId) {
        return new ActualizarCuadrillaCommand(UUID.fromString(cuadrillaId),
                nuevoLiderId != null ? UUID.fromString(nuevoLiderId) : null,
                agregarMiembroId != null ? UUID.fromString(agregarMiembroId) : null,
                removerMiembroId != null ? UUID.fromString(removerMiembroId) : null, null // inactivar not exposed in
                                                                                          // this request
        );
    }
}
