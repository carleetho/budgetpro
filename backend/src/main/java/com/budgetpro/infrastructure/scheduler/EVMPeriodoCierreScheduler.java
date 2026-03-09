package com.budgetpro.infrastructure.scheduler;

import com.budgetpro.application.finanzas.evm.port.in.CerrarPeriodoUseCase;
import com.budgetpro.domain.finanzas.evm.port.out.EVMTimeSeriesRepository;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Scheduler que cierra períodos de valuación EVM diariamente a las 00:05 UTC (REQ-64, AC-E04-INV-04).
 *
 * <p>Para cada proyecto con frecuenciaControl configurada, valida si hoy es una fecha de corte
 * alineada y, si no existe ya un registro en evm_time_series, invoca CerrarPeriodoUseCase.cerrar().
 *
 * <p>Duplicados: se evitan consultando existsByProyectoIdAndFechaCorte antes de cerrar.
 */
@Component
public class EVMPeriodoCierreScheduler {

    private static final Logger log = LoggerFactory.getLogger(EVMPeriodoCierreScheduler.class);

    private final ProyectoRepository proyectoRepository;
    private final EVMTimeSeriesRepository evmTimeSeriesRepository;
    private final CerrarPeriodoUseCase cerrarPeriodoUseCase;

    public EVMPeriodoCierreScheduler(
            ProyectoRepository proyectoRepository,
            EVMTimeSeriesRepository evmTimeSeriesRepository,
            CerrarPeriodoUseCase cerrarPeriodoUseCase) {
        this.proyectoRepository = proyectoRepository;
        this.evmTimeSeriesRepository = evmTimeSeriesRepository;
        this.cerrarPeriodoUseCase = cerrarPeriodoUseCase;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "UTC")
    public void cerrarPeriodosVencidos() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<Proyecto> proyectos = proyectoRepository.findAllWithFrecuenciaControl();

        for (Proyecto proyecto : proyectos) {
            try {
                processProject(proyecto, today);
            } catch (Exception ex) {
                log.error("Error cerrando periodo para proyecto {}: {}",
                        proyecto.getId().getValue(),
                        ex.getMessage(),
                        ex);
            }
        }
    }

    private void processProject(Proyecto proyecto, LocalDate today) {
        var proyectoId = proyecto.getId().getValue();

        if (!proyecto.esFechaCorteValida(today)) {
            log.warn("Periodo omitido para proyecto {} — fecha de corte no alineada con frecuencia {}",
                    proyectoId, proyecto.getFrecuenciaControl().name());
            return;
        }

        if (evmTimeSeriesRepository.existsByProyectoIdAndFechaCorte(proyectoId, today)) {
            log.warn("Periodo omitido para proyecto {} — ya existe cierre para fecha {}",
                    proyectoId, today);
            return;
        }

        cerrarPeriodoUseCase.cerrar(proyectoId, today);
        log.info("Periodo cerrado para proyecto {} con frecuencia {}",
                proyectoId,
                proyecto.getFrecuenciaControl().name());
    }
}
