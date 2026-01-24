package com.budgetpro.infrastructure.adapter.stub;

import com.budgetpro.domain.logistica.inventario.port.out.PartidaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PartidaRepositoryStub implements PartidaRepository {
    @Override
    public boolean existsById(UUID partidaId) {
        return true;
    }

    @Override
    public boolean isPresupuestoCongelado(UUID partidaId) {
        return true;
    }
}
