package com.budgetpro.application.partida.port.in;

import com.budgetpro.application.partida.dto.PartidaResponse;

import java.util.UUID;

public interface ObtenerPartidaUseCase {
    PartidaResponse obtenerPorId(UUID partidaId);
}

