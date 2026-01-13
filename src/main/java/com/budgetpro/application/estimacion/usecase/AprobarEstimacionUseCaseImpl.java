package com.budgetpro.application.estimacion.usecase;

import com.budgetpro.application.estimacion.port.in.AprobarEstimacionUseCase;
import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.port.out.EstimacionRepository;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación del caso de uso para aprobar una estimación.
 * 
 * CRÍTICO: Cuando se aprueba una estimación, se registra el ingreso en la billetera.
 */
@Service
public class AprobarEstimacionUseCaseImpl implements AprobarEstimacionUseCase {

    private final EstimacionRepository estimacionRepository;
    private final BilleteraRepository billeteraRepository;

    public AprobarEstimacionUseCaseImpl(EstimacionRepository estimacionRepository,
                                       BilleteraRepository billeteraRepository) {
        this.estimacionRepository = estimacionRepository;
        this.billeteraRepository = billeteraRepository;
    }

    @Override
    @Transactional
    public void aprobar(UUID estimacionId) {
        // 1. Buscar la estimación
        Estimacion estimacion = estimacionRepository.findById(EstimacionId.of(estimacionId))
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No se encontró una estimación con ID %s", estimacionId)));

        // 2. Aprobar la estimación (cambia estado a APROBADA)
        estimacion.aprobar();

        // 3. Persistir estimación aprobada
        estimacionRepository.save(estimacion);

        // 4. CRÍTICO: Registrar ingreso en la billetera del proyecto
        // Buscar o crear billetera del proyecto
        Billetera billetera = billeteraRepository.findByProyectoId(estimacion.getProyectoId())
                .orElseGet(() -> {
                    BilleteraId id = BilleteraId.generate();
                    return Billetera.crear(id, estimacion.getProyectoId());
                });

        // Registrar ingreso por el monto neto a pagar
        String referencia = String.format("Estimación %d - Proyecto %s", 
                                         estimacion.getNumeroEstimacion(), estimacion.getProyectoId());
        billetera.ingresar(estimacion.getMontoNetoPagar(), referencia, null);

        // Persistir billetera (esto también persistirá el movimiento de caja)
        billeteraRepository.save(billetera);
    }
}
