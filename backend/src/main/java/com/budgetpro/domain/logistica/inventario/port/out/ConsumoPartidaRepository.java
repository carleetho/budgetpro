package com.budgetpro.domain.logistica.inventario.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface ConsumoPartidaRepository {
    void registrarConsumo(UUID partidaId, BigDecimal montoAC, LocalDateTime fecha, String referencia);
}
