package com.budgetpro.application.evm.service;

import com.budgetpro.domain.finanzas.evm.model.EVMSnapshot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicación para el cálculo y consulta de métricas EVM.
 */
public interface EVMCalculationService {

    /**
     * Calcula y persiste un snapshot de EVM para un proyecto y fecha de corte.
     */
    EVMSnapshot calcularYPersistir(UUID proyectoId, LocalDateTime fechaCorte);

    /**
     * Obtiene el snapshot de EVM más reciente de un proyecto.
     */
    EVMSnapshot obtenerUltimo(UUID proyectoId);

    /**
     * Obtiene el histórico de snapshots de EVM para un proyecto.
     */
    List<EVMSnapshot> obtenerHistorico(UUID proyectoId, LocalDateTime desde, LocalDateTime hasta);
}
