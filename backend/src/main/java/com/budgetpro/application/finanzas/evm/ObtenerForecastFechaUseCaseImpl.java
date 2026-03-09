package com.budgetpro.application.finanzas.evm;

import com.budgetpro.application.finanzas.evm.port.in.ForecastResult;
import com.budgetpro.application.finanzas.evm.port.in.ObtenerForecastFechaUseCase;
import com.budgetpro.application.finanzas.evm.port.in.ProyectoNotFoundException;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.domain.finanzas.evm.model.EVMTimeSeries;
import com.budgetpro.domain.finanzas.evm.port.out.EVMTimeSeriesRepository;
import com.budgetpro.domain.finanzas.evm.util.WorkingDayCalculator;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del caso de uso para obtener la fecha de finalización proyectada (REQ-63, UC-E05).
 */
@Service
public class ObtenerForecastFechaUseCaseImpl implements ObtenerForecastFechaUseCase {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SPI_SCALE = 4;

    private final ProyectoRepository proyectoRepository;
    private final ProgramaObraRepository programaObraRepository;
    private final EVMTimeSeriesRepository evmTimeSeriesRepository;
    private final WorkingDayCalculator workingDayCalculator;

    public ObtenerForecastFechaUseCaseImpl(
            ProyectoRepository proyectoRepository,
            ProgramaObraRepository programaObraRepository,
            EVMTimeSeriesRepository evmTimeSeriesRepository,
            WorkingDayCalculator workingDayCalculator) {
        this.proyectoRepository = proyectoRepository;
        this.programaObraRepository = programaObraRepository;
        this.evmTimeSeriesRepository = evmTimeSeriesRepository;
        this.workingDayCalculator = workingDayCalculator;
    }

    @Override
    @Transactional(readOnly = true)
    public ForecastResult obtener(UUID proyectoId) {
        if (!proyectoRepository.existsById(ProyectoId.from(proyectoId))) {
            throw new ProyectoNotFoundException(proyectoId);
        }

        LocalDate fechaFinPlanificada = programaObraRepository.findByProyectoId(proyectoId)
                .map(ProgramaObra::getFechaFinEstimada)
                .orElse(null);

        Optional<EVMTimeSeries> latest = evmTimeSeriesRepository.findLatestByProyectoId(proyectoId);

        if (latest.isEmpty()) {
            return buildFallback(proyectoId, null, fechaFinPlanificada);
        }

        EVMTimeSeries ts = latest.get();
        BigDecimal pvAcumulado = ts.getPvAcumulado();
        if (pvAcumulado == null || pvAcumulado.compareTo(ZERO) == 0) {
            return buildFallback(proyectoId, ts.getFechaCorte(), fechaFinPlanificada);
        }

        if (fechaFinPlanificada == null || ts.getFechaCorte().equals(fechaFinPlanificada)
                || ts.getFechaCorte().isAfter(fechaFinPlanificada)) {
            return buildFallback(proyectoId, ts.getFechaCorte(), fechaFinPlanificada);
        }

        BigDecimal evAcumulado = ts.getEvAcumulado() != null ? ts.getEvAcumulado() : ZERO;
        BigDecimal spiAcumulado = evAcumulado.divide(pvAcumulado, SPI_SCALE, RoundingMode.HALF_UP);

        if (spiAcumulado.compareTo(ZERO) <= 0) {
            return buildFallback(proyectoId, ts.getFechaCorte(), fechaFinPlanificada);
        }

        int remainingDays = workingDayCalculator.workingDaysBetween(ts.getFechaCorte(), fechaFinPlanificada);
        int actualRemainingWorkingDays = new BigDecimal(remainingDays)
                .divide(spiAcumulado, 0, RoundingMode.CEILING)
                .intValue();

        LocalDate forecastCompletionDate = workingDayCalculator.plusWorkingDays(ts.getFechaCorte(), actualRemainingWorkingDays);

        return new ForecastResult(
                proyectoId,
                ts.getFechaCorte(),
                forecastCompletionDate,
                fechaFinPlanificada,
                remainingDays,
                spiAcumulado,
                false);
    }

    private ForecastResult buildFallback(UUID proyectoId, LocalDate fechaCorteBase, LocalDate fechaFinPlanificada) {
        LocalDate forecastCompletionDate = fechaFinPlanificada;
        return new ForecastResult(
                proyectoId,
                fechaCorteBase,
                forecastCompletionDate,
                fechaFinPlanificada,
                0,
                ZERO.setScale(SPI_SCALE, RoundingMode.HALF_UP),
                true);
    }
}
