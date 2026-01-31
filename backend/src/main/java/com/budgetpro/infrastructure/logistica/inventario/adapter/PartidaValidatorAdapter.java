package com.budgetpro.infrastructure.logistica.inventario.adapter;

import com.budgetpro.domain.logistica.inventario.port.out.PartidaValidator;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PartidaValidatorAdapter implements PartidaValidator {

    private final PartidaJpaRepository repository;

    public PartidaValidatorAdapter(PartidaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existeYEstaActiva(UUID partidaId) {
        if (partidaId == null)
            return false;
        return repository.existsById(partidaId);
    }

    @Override
    public double getPorcentajeEjecucion(UUID partidaId) {
        // Mock implementation for startup
        return 0.0;
    }
}
