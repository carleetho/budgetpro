package com.budgetpro.application.finanzas.evm;

import com.budgetpro.application.finanzas.evm.port.in.ObtenerSCurveUseCase;
import com.budgetpro.application.finanzas.evm.port.in.ProyectoNotFoundException;
import com.budgetpro.application.finanzas.evm.port.in.SCurveResult;
import com.budgetpro.domain.finanzas.evm.model.EVMTimeSeries;
import com.budgetpro.domain.finanzas.evm.port.out.EVMDataProvider;
import com.budgetpro.domain.finanzas.evm.port.out.EVMTimeSeriesRepository;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObtenerSCurveUseCaseImpl implements ObtenerSCurveUseCase {

    private final EVMTimeSeriesRepository evmTimeSeriesRepository;
    private final EVMDataProvider evmDataProvider;
    private final ProyectoRepository proyectoRepository;

    public ObtenerSCurveUseCaseImpl(
            EVMTimeSeriesRepository evmTimeSeriesRepository,
            EVMDataProvider evmDataProvider,
            ProyectoRepository proyectoRepository) {
        this.evmTimeSeriesRepository = evmTimeSeriesRepository;
        this.evmDataProvider = evmDataProvider;
        this.proyectoRepository = proyectoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SCurveResult obtener(UUID proyectoId, LocalDate startDate, LocalDate endDate) {
        validarProyectoExiste(proyectoId);

        List<EVMTimeSeries> timeSeries = evmTimeSeriesRepository.findByProyectoId(proyectoId, startDate, endDate);
        BigDecimal bacAjustado = evmDataProvider.getAdjustedBudgetAtCompletion(proyectoId);

        if (timeSeries.isEmpty()) {
            BigDecimal bacTotal = evmDataProvider.getBudgetAtCompletion(proyectoId);
            return new SCurveResult(proyectoId, null, bacTotal, bacAjustado, List.of());
        }

        List<EVMTimeSeries> orderedSeries = timeSeries.stream()
                .sorted(Comparator.comparing(EVMTimeSeries::getFechaCorte))
                .toList();

        EVMTimeSeries firstRow = orderedSeries.get(0);
        List<SCurveResult.SCurveDataPoint> dataPoints = orderedSeries.stream()
                .map(this::toDataPoint)
                .toList();

        return new SCurveResult(
                proyectoId,
                firstRow.getMoneda(),
                firstRow.getBacTotal(),
                bacAjustado,
                dataPoints);
    }

    private void validarProyectoExiste(UUID proyectoId) {
        if (!proyectoRepository.existsById(ProyectoId.from(proyectoId))) {
            throw new ProyectoNotFoundException(proyectoId);
        }
    }

    private SCurveResult.SCurveDataPoint toDataPoint(EVMTimeSeries row) {
        return new SCurveResult.SCurveDataPoint(
                row.getFechaCorte(),
                row.getPeriodo(),
                row.getPvAcumulado(),
                row.getEvAcumulado(),
                row.getAcAcumulado(),
                row.getCpiPeriodo() != null ? row.getCpiPeriodo() : BigDecimal.ZERO,
                row.getSpiPeriodo() != null ? row.getSpiPeriodo() : BigDecimal.ZERO);
    }
}
