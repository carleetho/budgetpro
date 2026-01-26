package com.budgetpro.application.estimacion.port.in;

import com.budgetpro.application.estimacion.dto.AvancePartidaResponse;
import java.util.UUID;

public interface ConsultarAvancePartidaUseCase {
    AvancePartidaResponse consultar(UUID partidaId, UUID proyectoId);
}
