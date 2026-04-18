package com.budgetpro.domain.finanzas.sobrecosto.port.out;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Lectura del factor salarial (FSR) efectivo desde la configuración laboral vigente (RRHH extendido).
 */
public interface LaborFsRReaderPort {

    Optional<BigDecimal> findFsRForProyecto(UUID proyectoId);

    Optional<BigDecimal> findFsRGlobal();
}
