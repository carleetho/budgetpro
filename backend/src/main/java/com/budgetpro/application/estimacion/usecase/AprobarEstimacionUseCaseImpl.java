package com.budgetpro.application.estimacion.usecase;

import com.budgetpro.application.estimacion.port.in.AprobarEstimacionUseCase;
import com.budgetpro.domain.finanzas.anticipo.model.AnticipoMovimiento;
import com.budgetpro.domain.finanzas.anticipo.port.out.AnticipoMovimientoRepository;
import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.port.out.EstimacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación del caso de uso para aprobar una estimación.
 * 
 * CRÍTICO: Cuando se aprueba una estimación, no se registra ingreso sin
 * evidencia.
 */
@Service
public class AprobarEstimacionUseCaseImpl implements AprobarEstimacionUseCase {

    private final EstimacionRepository estimacionRepository;
    private final AnticipoMovimientoRepository anticipoMovimientoRepository;

    public AprobarEstimacionUseCaseImpl(EstimacionRepository estimacionRepository,
            AnticipoMovimientoRepository anticipoMovimientoRepository) {
        this.estimacionRepository = estimacionRepository;
        this.anticipoMovimientoRepository = anticipoMovimientoRepository;
    }

    @Override
    @Transactional
    public void aprobar(UUID estimacionId, UUID aprobadoPor) {
        // 1. Buscar la estimación
        Estimacion estimacion = estimacionRepository.findById(EstimacionId.of(estimacionId))
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No se encontró una estimación con ID %s", estimacionId)));

        // 2. Validar evidencia contractual obligatoria antes de aprobar
        if (estimacion.getEvidenciaUrl() == null || estimacion.getEvidenciaUrl().isBlank()) {
            throw new IllegalStateException(
                    String.format("No se puede aprobar la estimación %d sin evidencia contractual válida",
                            estimacion.getNumeroEstimacion()));
        }

        // 3. Aprobar la estimación (cambia estado a APROBADA)
        // NOTA: Los JSON de items, totales y metadata se generan internamente en el
        // dominio
        estimacion.aprobar(aprobadoPor, null, null, null);

        // 4. Persistir estimación aprobada
        estimacionRepository.save(estimacion);

        // 5. Registrar amortización de anticipo si aplica
        if (estimacion.getAmortizacionAnticipo() != null
                && estimacion.getAmortizacionAnticipo().compareTo(java.math.BigDecimal.ZERO) > 0) {
            AnticipoMovimiento movimiento = AnticipoMovimiento.amortizar(estimacion.getPresupuestoId().getValue(),
                    estimacion.getAmortizacionAnticipo(),
                    String.format("Amortización Estimación %d", estimacion.getNumeroEstimacion()));
            anticipoMovimientoRepository.registrar(movimiento);
        }
    }
}
