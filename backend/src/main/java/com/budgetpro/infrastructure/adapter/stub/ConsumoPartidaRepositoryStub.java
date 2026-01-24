package com.budgetpro.infrastructure.adapter.stub;

import com.budgetpro.domain.logistica.inventario.port.out.ConsumoPartidaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ConsumoPartidaRepositoryStub implements ConsumoPartidaRepository {
    @Override
    public void registrarConsumo(UUID partidaId, BigDecimal montoAC, LocalDateTime fecha, String referencia) {
        // No-op
    }
}
