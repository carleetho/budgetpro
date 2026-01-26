package com.budgetpro.application.finanzas.evm.port.in;

import com.budgetpro.domain.finanzas.evm.model.EVMSnapshot;
import java.util.UUID;

public interface GenerarEVMSnapshotUseCase {

    /**
     * Calcula y genera un snapshot de EVM para un proyecto específico.
     *
     * @param proyectoId Identificador del proyecto
     * @return El snapshot generado con todas las métricas calculadas
     */
    EVMSnapshot generar(UUID proyectoId);
}
