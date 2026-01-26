package com.budgetpro.application.estimacion.port.out;

import com.budgetpro.application.estimacion.dto.ConsumoMaterialResponse;
import java.time.LocalDate;
import java.util.UUID;

public interface InventarioIntegrationPort {
    ConsumoMaterialResponse consultarConsumo(UUID proyectoId, UUID partidaId, LocalDate inicio, LocalDate fin);
}
