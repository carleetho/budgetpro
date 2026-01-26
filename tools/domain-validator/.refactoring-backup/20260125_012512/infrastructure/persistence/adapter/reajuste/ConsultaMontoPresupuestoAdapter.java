package com.budgetpro.infrastructure.persistence.adapter.reajuste;

import com.budgetpro.application.reajuste.usecase.CalcularReajusteUseCaseImpl;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.apu.ApuEntity;
import com.budgetpro.infrastructure.persistence.entity.apu.ApuInsumoEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.apu.ApuJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.apu.ApuInsumoJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación de ConsultaMontoPresupuesto que consulta montos del presupuesto.
 */
@Component
public class ConsultaMontoPresupuestoAdapter implements CalcularReajusteUseCaseImpl.ConsultaMontoPresupuesto {

    private final PartidaJpaRepository partidaJpaRepository;
    private final ApuJpaRepository apuJpaRepository;
    private final ApuInsumoJpaRepository apuInsumoJpaRepository;

    public ConsultaMontoPresupuestoAdapter(
            PartidaJpaRepository partidaJpaRepository,
            ApuJpaRepository apuJpaRepository,
            ApuInsumoJpaRepository apuInsumoJpaRepository) {
        this.partidaJpaRepository = partidaJpaRepository;
        this.apuJpaRepository = apuJpaRepository;
        this.apuInsumoJpaRepository = apuInsumoJpaRepository;
    }

    @Override
    public BigDecimal obtenerMontoBase(UUID presupuestoId) {
        List<PartidaEntity> partidas = partidaJpaRepository.findByPresupuestoId(presupuestoId);
        
        return partidas.stream()
                .map(this::calcularMontoPartida)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<DetallePartidaMonto> obtenerDetallesPartidas(UUID presupuestoId) {
        List<PartidaEntity> partidas = partidaJpaRepository.findByPresupuestoId(presupuestoId);
        
        return partidas.stream()
                .map(partida -> {
                    BigDecimal montoBase = calcularMontoPartida(partida);
                    return new DetallePartidaMonto(partida.getId(), montoBase);
                })
                .collect(Collectors.toList());
    }

    /**
     * Calcula el monto base de una partida: metrado × costo unitario del APU.
     */
    private BigDecimal calcularMontoPartida(PartidaEntity partida) {
        // Si es título (metrado = 0), retornar 0
        if (partida.getMetrado().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Buscar APU de la partida
        Optional<ApuEntity> apuOpt = apuJpaRepository.findByPartidaId(partida.getId());
        if (apuOpt.isEmpty()) {
            return BigDecimal.ZERO; // Si no tiene APU, no tiene costo
        }

        // Calcular costo unitario del APU (suma de subtotales de insumos)
        ApuEntity apu = apuOpt.get();
        List<ApuInsumoEntity> insumos = apuInsumoJpaRepository.findByApuId(apu.getId());
        
        BigDecimal costoUnitario = insumos.stream()
                .map(ApuInsumoEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Monto base = metrado × costo unitario
        return partida.getMetrado().multiply(costoUnitario);
    }
}
