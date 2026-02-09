package com.budgetpro.application.reajuste.usecase;

import com.budgetpro.application.reajuste.dto.EstimacionReajusteResponse;
import com.budgetpro.application.reajuste.port.in.CalcularReajusteUseCase;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.reajuste.model.*;
import com.budgetpro.domain.finanzas.reajuste.port.out.EstimacionReajusteRepository;
import com.budgetpro.domain.finanzas.reajuste.port.out.IndicePreciosRepository;
import com.budgetpro.domain.finanzas.reajuste.service.CalculadorReajusteService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para calcular reajuste de costos.
 */
@Service
public class CalcularReajusteUseCaseImpl implements CalcularReajusteUseCase {

    private final CalculadorReajusteService calculadorService;
    private final EstimacionReajusteRepository estimacionRepository;
    private final IndicePreciosRepository indiceRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final ConsultaMontoPresupuesto consultaMontoPresupuesto;

    public CalcularReajusteUseCaseImpl(CalculadorReajusteService calculadorService,
            EstimacionReajusteRepository estimacionRepository, IndicePreciosRepository indiceRepository,
            PresupuestoRepository presupuestoRepository, ConsultaMontoPresupuesto consultaMontoPresupuesto) {
        this.calculadorService = calculadorService;
        this.estimacionRepository = estimacionRepository;
        this.indiceRepository = indiceRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.consultaMontoPresupuesto = consultaMontoPresupuesto;
    }

    @Override
    @Transactional
    public EstimacionReajusteResponse calcular(UUID proyectoId, UUID presupuestoId, LocalDate fechaCorte,
            String indiceBaseCodigo, LocalDate indiceBaseFecha, String indiceActualCodigo,
            LocalDate indiceActualFecha) {
        // Validar que el presupuesto existe
        presupuestoRepository.findById(PresupuestoId.from(presupuestoId))
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado: " + presupuestoId));

        // Buscar índices
        IndicePrecios indiceBase = indiceRepository.buscarPorCodigoYFecha(indiceBaseCodigo, indiceBaseFecha)
                .orElseThrow(() -> new IllegalArgumentException(String
                        .format("Índice base no encontrado: %s para fecha %s", indiceBaseCodigo, indiceBaseFecha)));

        IndicePrecios indiceActual = indiceRepository.buscarPorCodigoYFecha(indiceActualCodigo, indiceActualFecha)
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Índice actual no encontrado: %s para fecha %s", indiceActualCodigo, indiceActualFecha)));

        // Obtener monto base del presupuesto
        BigDecimal montoBase = consultaMontoPresupuesto.obtenerMontoBase(presupuestoId);

        // Obtener siguiente número de estimación
        Integer numeroEstimacion = estimacionRepository.obtenerSiguienteNumeroEstimacion(proyectoId);

        // Crear estimación de reajuste
        EstimacionReajusteId estimacionId = EstimacionReajusteId.generate();
        EstimacionReajuste estimacion = EstimacionReajuste.crear(estimacionId, proyectoId, presupuestoId,
                numeroEstimacion, fechaCorte, indiceBaseCodigo, indiceBaseFecha, indiceActualCodigo, indiceActualFecha,
                indiceBase.getValor(), indiceActual.getValor(), montoBase);

        // Calcular monto reajustado
        estimacion = estimacion.calcularMontoReajustado(montoBase);

        // Calcular reajuste por partida
        List<DetalleReajustePartida> detalles = consultaMontoPresupuesto.obtenerDetallesPartidas(presupuestoId).stream()
                .map(detalle -> {
                    BigDecimal montoReajustadoPartida = calculadorService.calcularPrecioReajustado(detalle.montoBase(),
                            indiceBase.getValor(), indiceActual.getValor());
                    BigDecimal diferencialPartida = calculadorService.calcularDiferencial(montoReajustadoPartida,
                            detalle.montoBase());

                    return DetalleReajustePartida.crear(DetalleReajustePartidaId.generate(), detalle.partidaId(),
                            detalle.montoBase(), montoReajustadoPartida, diferencialPartida);
                }).collect(Collectors.toList());

        for (DetalleReajustePartida detalle : detalles) {
            estimacion = estimacion.agregarDetalle(detalle);
        }

        // Persistir estimación
        estimacionRepository.guardar(estimacion);

        // Mapear a DTO de respuesta
        return mapearAResponse(estimacion);
    }

    private EstimacionReajusteResponse mapearAResponse(EstimacionReajuste estimacion) {
        List<EstimacionReajusteResponse.DetalleReajustePartidaResponse> detallesResponse = estimacion.getDetalles()
                .stream()
                .map(detalle -> new EstimacionReajusteResponse.DetalleReajustePartidaResponse(
                        detalle.getId().getValue(), detalle.getPartidaId(), detalle.getMontoBase(),
                        detalle.getMontoReajustado(), detalle.getDiferencial()))
                .collect(Collectors.toList());

        return new EstimacionReajusteResponse(estimacion.getId().getValue(), estimacion.getProyectoId(),
                estimacion.getPresupuestoId(), estimacion.getNumeroEstimacion(), estimacion.getFechaCorte(),
                estimacion.getIndiceBaseCodigo(), estimacion.getIndiceBaseFecha(), estimacion.getIndiceActualCodigo(),
                estimacion.getIndiceActualFecha(), estimacion.getValorIndiceBase(), estimacion.getValorIndiceActual(),
                estimacion.getMontoBase(), estimacion.getMontoReajustado(), estimacion.getDiferencial(),
                estimacion.getPorcentajeVariacion(), estimacion.getEstado().name(), estimacion.getObservaciones(),
                detallesResponse);
    }

    /**
     * Interfaz para consultar montos del presupuesto. Se implementará en la capa de
     * infraestructura.
     */
    public interface ConsultaMontoPresupuesto {
        BigDecimal obtenerMontoBase(UUID presupuestoId);

        List<DetallePartidaMonto> obtenerDetallesPartidas(UUID presupuestoId);

        record DetallePartidaMonto(UUID partidaId, BigDecimal montoBase) {
        }
    }
}
