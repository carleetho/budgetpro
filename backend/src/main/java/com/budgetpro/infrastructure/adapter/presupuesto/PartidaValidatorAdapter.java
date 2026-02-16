package com.budgetpro.infrastructure.adapter.presupuesto;

import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.logistica.compra.port.out.PartidaValidator;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Adaptador de infraestructura para PartidaValidator.
 * 
 * Valida que las partidas sean leaf nodes válidas según REGLA-153.
 */
@Component
public class PartidaValidatorAdapter implements PartidaValidator {

    private final PartidaRepository partidaRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final PartidaJpaRepository partidaJpaRepository;

    public PartidaValidatorAdapter(PartidaRepository partidaRepository,
                                  PresupuestoRepository presupuestoRepository,
                                  PartidaJpaRepository partidaJpaRepository) {
        this.partidaRepository = partidaRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.partidaJpaRepository = partidaJpaRepository;
    }

    @Override
    public boolean esPartidaLeafValida(UUID partidaId) {
        // 1. Verificar que la partida existe
        Partida partida = partidaRepository.findById(partidaId)
                .orElse(null);

        if (partida == null) {
            return false;
        }

        // 2. Verificar que el presupuesto esté congelado (frozen budget)
        Presupuesto presupuesto = presupuestoRepository.findById(
                com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId.from(partida.getPresupuestoId())
        ).orElse(null);

        if (presupuesto == null || presupuesto.getEstado() != EstadoPresupuesto.CONGELADO) {
            return false;
        }

        // 3. Verificar que la partida es una leaf node (no tiene hijos)
        // REGLA-153: Solo partidas hoja pueden ser utilizadas en compras
        List<com.budgetpro.infrastructure.persistence.entity.PartidaEntity> hijos = 
                partidaJpaRepository.findByPadreId(partidaId);

        return hijos.isEmpty();
    }
}
