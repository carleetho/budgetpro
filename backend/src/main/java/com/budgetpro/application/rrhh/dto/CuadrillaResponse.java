package com.budgetpro.application.rrhh.dto;

import com.budgetpro.domain.rrhh.model.EstadoCuadrilla;
import java.util.List;
import java.util.UUID;

public record CuadrillaResponse(UUID id, UUID proyectoId, String nombre, String tipo, UUID liderId,
        EstadoCuadrilla estado, List<UUID> miembrosIds) {
}
