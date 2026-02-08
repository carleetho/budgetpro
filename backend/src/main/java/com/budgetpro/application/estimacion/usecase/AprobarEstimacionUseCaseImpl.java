package com.budgetpro.application.estimacion.usecase;

import com.budgetpro.application.estimacion.port.in.AprobarEstimacionUseCase;
import com.budgetpro.domain.finanzas.anticipo.model.AnticipoMovimiento;
import com.budgetpro.domain.finanzas.anticipo.port.out.AnticipoMovimientoRepository;
import com.budgetpro.domain.finanzas.estimacion.exception.SequentialApprovalException;
import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion;
import com.budgetpro.domain.finanzas.estimacion.port.out.EstimacionRepository;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementación del caso de uso para aprobar una estimación.
 * 
 * CRÍTICO: Cuando se aprueba una estimación, se registra: 1. Amortización de
 * anticipo (si aplica) 2. INGRESO en la Billetera del proyecto (monto neto
 * después de amortización)
 */
@Service
public class AprobarEstimacionUseCaseImpl implements AprobarEstimacionUseCase {

        private final EstimacionRepository estimacionRepository;
        private final AnticipoMovimientoRepository anticipoMovimientoRepository;
        private final BilleteraRepository billeteraRepository;

        public AprobarEstimacionUseCaseImpl(EstimacionRepository estimacionRepository,
                        AnticipoMovimientoRepository anticipoMovimientoRepository,
                        BilleteraRepository billeteraRepository) {
                this.estimacionRepository = estimacionRepository;
                this.anticipoMovimientoRepository = anticipoMovimientoRepository;
                this.billeteraRepository = billeteraRepository;
        }

        @Override
        @Transactional
        public void aprobar(UUID estimacionId) {
                // 1. Buscar la estimación
                Estimacion estimacion = estimacionRepository.findById(EstimacionId.of(estimacionId))
                                .orElseThrow(() -> new IllegalArgumentException(String
                                                .format("No se encontró una estimación con ID %s", estimacionId)));

                // 2. ES-01: Validar aprobación secuencial (AXIOM Critical Fix)
                if (estimacion.getNumeroEstimacion() > 1) {
                        Integer numeroPredecesor = estimacion.getNumeroEstimacion() - 1;

                        Estimacion estimacionPredecesora = estimacionRepository
                                        .findByProyectoIdAndNumero(estimacion.getProyectoId(), numeroPredecesor)
                                        .orElseThrow(() -> new SequentialApprovalException(estimacion.getProyectoId(),
                                                        estimacion.getNumeroEstimacion(), numeroPredecesor,
                                                        String.format("Cannot approve estimation #%d: Previous estimation #%d not found",
                                                                        estimacion.getNumeroEstimacion(),
                                                                        numeroPredecesor)));

                        if (estimacionPredecesora.getEstado() != EstadoEstimacion.APROBADA) {
                                throw new SequentialApprovalException(estimacion.getProyectoId(),
                                                estimacion.getNumeroEstimacion(), numeroPredecesor,
                                                String.format("Cannot approve estimation #%d: Previous estimation #%d is %s (must be APROBADA)",
                                                                estimacion.getNumeroEstimacion(), numeroPredecesor,
                                                                estimacionPredecesora.getEstado()));
                        }
                }

                // 3. Validar evidencia contractual obligatoria antes de aprobar

                if (estimacion.getEvidenciaUrl() == null || estimacion.getEvidenciaUrl().isBlank()) {
                        throw new IllegalStateException(String.format(
                                        "No se puede aprobar la estimación %d sin evidencia contractual válida",
                                        estimacion.getNumeroEstimacion()));
                }

                // 3. Aprobar la estimación (cambia estado a APROBADA)
                estimacion.aprobar();

                // 4. Persistir estimación aprobada
                estimacionRepository.save(estimacion);

                // 5. Registrar amortización de anticipo si aplica
                BigDecimal amortizacion = estimacion.getAmortizacionAnticipo() != null
                                ? estimacion.getAmortizacionAnticipo()
                                : BigDecimal.ZERO;

                if (amortizacion.compareTo(BigDecimal.ZERO) > 0) {
                        AnticipoMovimiento movimiento = AnticipoMovimiento.amortizar(estimacion.getProyectoId(),
                                        amortizacion,
                                        String.format("Amortización Estimación %d", estimacion.getNumeroEstimacion()));
                        anticipoMovimientoRepository.registrar(movimiento);
                }

                // 6. ES-02: Registrar INGRESO en Billetera (AXIOM Critical Fix)
                // El monto neto ya está calculado en la Estimación (bruto - amortización -
                // retención)
                BigDecimal montoNeto = estimacion.getMontoNetoPagar();

                if (montoNeto.compareTo(BigDecimal.ZERO) > 0) {
                        Billetera billetera = billeteraRepository.findByProyectoId(estimacion.getProyectoId())
                                        .orElseThrow(() -> new IllegalStateException(
                                                        String.format("No se encontró billetera para el proyecto %s",
                                                                        estimacion.getProyectoId())));

                        // Registrar ingreso con evidencia de la estimación aprobada
                        String referenciaIngreso = String.format("Estimación #%d Aprobada - Proyecto",
                                        estimacion.getNumeroEstimacion());

                        billetera.ingresar(montoNeto, referenciaIngreso, estimacion.getEvidenciaUrl());

                        // Persistir billetera con el nuevo movimiento
                        billeteraRepository.save(billetera);
                }
        }
}
