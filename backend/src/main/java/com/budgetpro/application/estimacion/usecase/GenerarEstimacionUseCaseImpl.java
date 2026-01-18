package com.budgetpro.application.estimacion.usecase;

import com.budgetpro.application.estimacion.dto.DetalleEstimacionResponse;
import com.budgetpro.application.estimacion.dto.EstimacionResponse;
import com.budgetpro.application.estimacion.dto.GenerarEstimacionCommand;
import com.budgetpro.application.estimacion.port.in.GenerarEstimacionUseCase;
import com.budgetpro.application.presupuesto.exception.ProyectoNoEncontradoException;
import com.budgetpro.domain.finanzas.anticipo.port.out.AnticipoMovimientoRepository;
import com.budgetpro.domain.finanzas.estimacion.model.DetalleEstimacion;
import com.budgetpro.domain.finanzas.estimacion.model.DetalleEstimacionId;
import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.port.out.EstimacionRepository;
import com.budgetpro.domain.finanzas.estimacion.service.GeneradorEstimacionService;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para generar una estimación.
 */
@Service
public class GenerarEstimacionUseCaseImpl implements GenerarEstimacionUseCase {

    private final ProyectoRepository proyectoRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final PartidaRepository partidaRepository;
    private final EstimacionRepository estimacionRepository;
    private final GeneradorEstimacionService generadorEstimacionService;
    private final AnticipoMovimientoRepository anticipoMovimientoRepository;

    public GenerarEstimacionUseCaseImpl(ProyectoRepository proyectoRepository,
                                      PresupuestoRepository presupuestoRepository,
                                      PartidaRepository partidaRepository,
                                      EstimacionRepository estimacionRepository,
                                      GeneradorEstimacionService generadorEstimacionService,
                                      AnticipoMovimientoRepository anticipoMovimientoRepository) {
        this.proyectoRepository = proyectoRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.partidaRepository = partidaRepository;
        this.estimacionRepository = estimacionRepository;
        this.generadorEstimacionService = generadorEstimacionService;
        this.anticipoMovimientoRepository = anticipoMovimientoRepository;
    }

    @Override
    @Transactional
    public EstimacionResponse generar(GenerarEstimacionCommand command) {
        // 1. Validar que el proyecto existe
        if (proyectoRepository.findById(ProyectoId.from(command.proyectoId())).isEmpty()) {
            throw new ProyectoNoEncontradoException(command.proyectoId());
        }

        // 2. Buscar el presupuesto del proyecto
        if (presupuestoRepository.findByProyectoId(command.proyectoId()).isEmpty()) {
            throw new IllegalStateException(
                    String.format("No existe presupuesto para el proyecto %s", command.proyectoId()));
        }

        // 3. Obtener el siguiente número de estimación
        Integer numeroEstimacion = estimacionRepository.obtenerSiguienteNumeroEstimacion(command.proyectoId());

        // 4. Buscar estimaciones previas aprobadas (para calcular acumulados)
        List<Estimacion> estimacionesPrevias = estimacionRepository.findAprobadasByProyectoId(command.proyectoId());

        // 5. Crear la estimación
        EstimacionId estimacionId = EstimacionId.nuevo();
        Estimacion estimacion = Estimacion.crear(estimacionId, command.proyectoId(), numeroEstimacion,
                command.fechaCorte(), command.periodoInicio(), command.periodoFin(),
                command.evidenciaUrl());

        // 6. Procesar cada detalle
        for (GenerarEstimacionCommand.DetalleEstimacionItem item : command.detalles()) {
            // 6.1 Validar que la partida existe
            Partida partida = partidaRepository.findById(PartidaId.from(item.partidaId()))
                    .orElseThrow(() -> new com.budgetpro.application.compra.exception.PartidaNoEncontradaException(item.partidaId()));

            // 6.2 Calcular acumulado anterior
            BigDecimal acumuladoAnterior = generadorEstimacionService.calcularAcumuladoAnterior(
                    item.partidaId(), estimacionesPrevias);

            // 6.3 Validar volumen (no permitir estimar más del 100%)
            if (!generadorEstimacionService.validarVolumenEstimado(
                    item.cantidadAvance(), acumuladoAnterior, partida.getMetrado())) {
                throw new IllegalArgumentException(
                        String.format("La cantidad estimada excede el volumen contratado para la partida %s. " +
                                     "Acumulado anterior: %s, Avance: %s, Volumen contratado: %s",
                                     item.partidaId(), acumuladoAnterior, item.cantidadAvance(), partida.getMetrado()));
            }

            // 6.4 Crear detalle
            DetalleEstimacionId detalleId = DetalleEstimacionId.nuevo();
            DetalleEstimacion detalle = DetalleEstimacion.crear(detalleId, item.partidaId(),
                    item.cantidadAvance(), item.precioUnitario(), acumuladoAnterior);

            estimacion.agregarDetalle(detalle);
        }

        // 7. Calcular amortización de anticipo
        BigDecimal amortizacionAnticipo = BigDecimal.ZERO;
        if (command.porcentajeAnticipo() != null && command.porcentajeAnticipo().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal saldoAnticipoPendiente = anticipoMovimientoRepository.obtenerSaldoPendiente(command.proyectoId());
            amortizacionAnticipo = generadorEstimacionService.calcularAmortizacionAnticipo(
                    estimacion.getMontoBruto(), command.porcentajeAnticipo(), saldoAnticipoPendiente);
        }
        estimacion.actualizarAmortizacionAnticipo(amortizacionAnticipo);

        // 8. Calcular retención de fondo de garantía
        BigDecimal retencionFondoGarantia = BigDecimal.ZERO;
        if (command.porcentajeRetencionFondoGarantia() != null &&
            command.porcentajeRetencionFondoGarantia().compareTo(BigDecimal.ZERO) > 0) {
            retencionFondoGarantia = generadorEstimacionService.calcularRetencionFondoGarantia(
                    estimacion.getMontoBruto(), command.porcentajeRetencionFondoGarantia());
        }
        estimacion.actualizarRetencionFondoGarantia(retencionFondoGarantia);

        // 9. Persistir estimación
        estimacionRepository.save(estimacion);

        // 10. Retornar respuesta
        List<DetalleEstimacionResponse> detallesResponse = estimacion.getDetalles().stream()
                .map(detalle -> new DetalleEstimacionResponse(
                    detalle.getId().getValue(),
                    detalle.getPartidaId(),
                    detalle.getCantidadAvance(),
                    detalle.getPrecioUnitario(),
                    detalle.getImporte(),
                    detalle.getAcumuladoAnterior()
                ))
                .collect(Collectors.toList());

        return new EstimacionResponse(
            estimacion.getId().getValue(),
            estimacion.getProyectoId(),
            estimacion.getNumeroEstimacion(),
            estimacion.getFechaCorte(),
            estimacion.getPeriodoInicio(),
            estimacion.getPeriodoFin(),
            estimacion.getMontoBruto(),
            estimacion.getAmortizacionAnticipo(),
            estimacion.getRetencionFondoGarantia(),
            estimacion.getMontoNetoPagar(),
            estimacion.getEvidenciaUrl(),
            estimacion.getEstado(),
            detallesResponse,
            estimacion.getVersion().intValue()
        );
    }
}
