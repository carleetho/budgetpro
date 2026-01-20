package com.budgetpro.application.apu.usecase;

import com.budgetpro.application.apu.exception.ApuNoEncontradoException;
import com.budgetpro.application.apu.port.in.ActualizarRendimientoUseCase;
import com.budgetpro.application.presupuesto.exception.PresupuestoNoEncontradoException;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.port.ApuSnapshotRepository;
import com.budgetpro.domain.catalogo.service.CalculoApuDinamicoService;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementación del caso de uso para actualizar el rendimiento de un APU.
 * 
 * Realiza recálculo automático en cascada de costos afectados y actualiza
 * el hash de ejecución si el presupuesto está aprobado.
 */
@Service
public class ActualizarRendimientoUseCaseImpl implements ActualizarRendimientoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ActualizarRendimientoUseCaseImpl.class);
    private static final String MONEDA_DEFAULT = "PEN"; // Soles peruanos por defecto

    private final ApuSnapshotRepository apuSnapshotRepository;
    private final CalculoApuDinamicoService calculoApuDinamicoService;
    private final PartidaRepository partidaRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final IntegrityHashService integrityHashService;

    public ActualizarRendimientoUseCaseImpl(
            ApuSnapshotRepository apuSnapshotRepository,
            CalculoApuDinamicoService calculoApuDinamicoService,
            PartidaRepository partidaRepository,
            PresupuestoRepository presupuestoRepository,
            IntegrityHashService integrityHashService) {
        this.apuSnapshotRepository = apuSnapshotRepository;
        this.calculoApuDinamicoService = calculoApuDinamicoService;
        this.partidaRepository = partidaRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.integrityHashService = integrityHashService;
    }

    @Override
    @Transactional
    public void actualizarRendimiento(UUID apuSnapshotId, BigDecimal nuevoRendimiento, UUID usuarioId) {
        // 1. Validar rendimiento
        if (nuevoRendimiento == null || nuevoRendimiento.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El rendimiento debe ser mayor a 0");
        }

        // 2. Obtener APUSnapshot
        APUSnapshot apuSnapshot = apuSnapshotRepository.findById(apuSnapshotId)
                .orElseThrow(() -> new ApuNoEncontradoException(apuSnapshotId));

        // 3. Guardar valor anterior para auditoría
        BigDecimal rendimientoAnterior = apuSnapshot.getRendimientoVigente();

        // 4. Actualizar rendimiento (dispara auditoría interna)
        apuSnapshot.actualizarRendimiento(nuevoRendimiento, usuarioId);

        // 5. Obtener partida y presupuesto para validar integridad si es necesario
        Partida partida = partidaRepository.findById(apuSnapshot.getPartidaId())
                .orElseThrow(() -> new IllegalStateException("Partida no encontrada para APU: " + apuSnapshotId));

        Presupuesto presupuesto = presupuestoRepository.findById(PresupuestoId.from(partida.getPresupuestoId()))
                .orElseThrow(() -> new PresupuestoNoEncontradoException(partida.getPresupuestoId()));

        // 6. Recalcular costo total del APU usando cálculo dinámico
        // Nota: La moneda se obtiene del proyecto, por ahora usamos default
        String monedaProyecto = MONEDA_DEFAULT; // TODO: Obtener del proyecto
        BigDecimal nuevoCostoTotal = apuSnapshot.calcularCostoTotal(calculoApuDinamicoService, monedaProyecto);

        // 7. Si el presupuesto está aprobado, validar integridad y actualizar hash de ejecución
        if (presupuesto.isAprobado()) {
            // Validar que no se haya modificado la estructura (solo se permite actualizar hash de ejecución)
            presupuesto.validarIntegridad(integrityHashService);
            
            // Actualizar hash de ejecución con el nuevo costo
            presupuesto.actualizarHashEjecucion(integrityHashService);
            
            presupuestoRepository.save(presupuesto);
            
            log.info("Rendimiento actualizado en presupuesto aprobado. APU: {}, Rendimiento: {} -> {}, Hash ejecución actualizado",
                    apuSnapshotId, rendimientoAnterior, nuevoRendimiento);
        } else {
            log.info("Rendimiento actualizado en presupuesto en edición. APU: {}, Rendimiento: {} -> {}",
                    apuSnapshotId, rendimientoAnterior, nuevoRendimiento);
        }

        // 8. Persistir cambios en APUSnapshot
        apuSnapshotRepository.save(apuSnapshot);

        log.debug("Costo total recalculado para APU {}: {} (moneda: {})", apuSnapshotId, nuevoCostoTotal, monedaProyecto);
    }
}
