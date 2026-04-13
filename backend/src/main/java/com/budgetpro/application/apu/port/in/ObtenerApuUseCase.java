package com.budgetpro.application.apu.port.in;

import com.budgetpro.application.apu.dto.ApuResponse;

import java.util.UUID;

public interface ObtenerApuUseCase {
    ApuResponse obtenerPorId(UUID apuId);
    ApuResponse obtenerPorPartidaId(UUID partidaId);
}

