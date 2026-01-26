package com.budgetpro.infrastructure.adapter.stub;

import com.budgetpro.domain.logistica.inventario.port.out.PartidaValidator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PartidaValidatorStub implements PartidaValidator {
    @Override
    public boolean existeYEstaActiva(UUID partidaId) {
        return true;
    }

    @Override
    public double getPorcentajeEjecucion(UUID partidaId) {
        return 0.0;
    }
}
