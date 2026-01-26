package com.budgetpro.domain.finanzas.estimacion.service;

import com.budgetpro.domain.finanzas.estimacion.exception.AvanceAcumuladoExcedidoException;
import com.budgetpro.domain.finanzas.estimacion.model.MontoEstimado;
import com.budgetpro.domain.finanzas.estimacion.model.PorcentajeAvance;
import com.budgetpro.domain.finanzas.estimacion.port.AvancePartidaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

public class AvanceCalculatorService {

    private final AvancePartidaRepository avancePartidaRepository;

    public AvanceCalculatorService(AvancePartidaRepository avancePartidaRepository) {
        this.avancePartidaRepository = Objects.requireNonNull(avancePartidaRepository,
                "El repositorio de avance de partida no puede ser nulo");
    }

    /**
     * Calcula el porcentaje acumulado histórico para una partida y valida que sumar
     * un nuevo porcentaje no exceda el 100%.
     */
    public void validateAvanceNoExcede100(UUID partidaId, BigDecimal nuevoPorcentaje) {
        Objects.requireNonNull(partidaId, "El partidaId no puede ser nulo");
        Objects.requireNonNull(nuevoPorcentaje, "El nuevo porcentaje no puede ser nulo");

        BigDecimal acumuladoActual = avancePartidaRepository.calcularAvanceAcumulado(partidaId);
        // Si no hay registros previos, el repositorio debería retornar 0
        if (acumuladoActual == null) {
            acumuladoActual = BigDecimal.ZERO;
        }

        BigDecimal total = acumuladoActual.add(nuevoPorcentaje);

        // 100.00 con 2 decimales
        BigDecimal maximo = new BigDecimal("100.00");

        if (total.compareTo(maximo) > 0) {
            throw new AvanceAcumuladoExcedidoException(partidaId, total.toString() + "%");
        }
    }

    /**
     * Calcula el monto estimado basado en cantidad ejecutada, precio unitario y/o
     * porcentaje. En este caso, trabajamos principalmente con porcentaje del monto
     * contractual.
     */
    public MontoEstimado calcularMontoEstimado(MontoEstimado montoContractual, PorcentajeAvance porcentajePeriodo) {
        Objects.requireNonNull(montoContractual, "El monto contractual no puede ser nulo");
        Objects.requireNonNull(porcentajePeriodo, "El porcentaje de avance no puede ser nulo");

        BigDecimal factor = porcentajePeriodo.getValue().divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        BigDecimal montoCalculado = montoContractual.getValue().multiply(factor);

        return MontoEstimado.of(montoCalculado);
    }
}
