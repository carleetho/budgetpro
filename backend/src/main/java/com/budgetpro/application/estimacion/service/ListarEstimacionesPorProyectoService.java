package com.budgetpro.application.estimacion.service;

import com.budgetpro.application.estimacion.dto.DetalleEstimacionResponse;
import com.budgetpro.application.estimacion.dto.EstimacionResponse;
import com.budgetpro.application.estimacion.port.in.ListarEstimacionesPorProyectoUseCase;
import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.port.EstimacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListarEstimacionesPorProyectoService implements ListarEstimacionesPorProyectoUseCase {

        private final EstimacionRepository estimacionRepository;
        private final com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository partidaRepository;

        public ListarEstimacionesPorProyectoService(EstimacionRepository estimacionRepository,
                        com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository partidaRepository) {
                this.estimacionRepository = estimacionRepository;
                this.partidaRepository = partidaRepository;
        }

        @Override
        public List<EstimacionResponse> listar(UUID proyectoId) {
                return estimacionRepository.findByProyectoId(proyectoId).stream().map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        private EstimacionResponse mapToResponse(Estimacion estimacion) {
                EstimacionResponse response = new EstimacionResponse();
                response.setId(estimacion.getId().getValue());
                response.setPresupuestoId(
                                estimacion.getPresupuestoId() != null ? estimacion.getPresupuestoId().getValue()
                                                : null);
                response.setEstado(estimacion.getEstado().name());
                response.setFechaInicio(estimacion.getPeriodo().getFechaInicio());
                response.setFechaFin(estimacion.getPeriodo().getFechaFin());

                // Handle potential nulls safely
                java.math.BigDecimal retencionPct = estimacion.getRetencionPorcentaje() != null
                                ? estimacion.getRetencionPorcentaje().getValue()
                                : java.math.BigDecimal.ZERO;
                response.setRetencionPorcentaje(retencionPct);

                response.setMontoRetencion(estimacion.calcularMontoRetencion().getValue());
                response.setSubtotal(estimacion.calcularSubtotal());
                response.setTotalPagar(estimacion.getMontoNetoPagar());
                response.setAmortizacionAnticipo(estimacion.getAmortizacionAnticipo());
                response.setRetencionFondoGarantia(estimacion.getRetencionFondoGarantia());

                response.setFechaCorte(estimacion.getFechaCorte());
                response.setNumeroEstimacion(estimacion.getNumeroEstimacion().longValue());
                response.setEvidenciaUrl(estimacion.getEvidenciaUrl());
                response.setVersion(estimacion.getVersion() != null ? estimacion.getVersion().intValue() : 0);

                List<DetalleEstimacionResponse> detalleResponses = estimacion.getDetalles().stream()
                                .map(this::mapDetalleToResponse).collect(Collectors.toList());
                response.setDetalles(detalleResponses);
                return response;
        }

        private DetalleEstimacionResponse mapDetalleToResponse(
                        com.budgetpro.domain.finanzas.estimacion.model.DetalleEstimacion item) {
                DetalleEstimacionResponse response = new DetalleEstimacionResponse();
                response.setId(item.getId().getValue());
                response.setPartidaId(item.getPartidaId());

                // Fetch Partida to get Contractual info
                // Note: This IS N+1. For high performance we should fetch all partidas in
                // batch.
                // Given constraints, we maintain integrity first.
                com.budgetpro.domain.finanzas.partida.model.Partida partida = partidaRepository.findById(
                                com.budgetpro.domain.finanzas.partida.model.PartidaId.from(item.getPartidaId()))
                                .orElse(null);

                String concepto = partida != null ? partida.getDescripcion() : "Desconocido"; // Assuming Partida has
                                                                                              // descripcion
                java.math.BigDecimal metradoTotal = partida != null ? partida.getMetrado() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal precioUnitario = item.getPrecioUnitario();
                java.math.BigDecimal montoContractual = metradoTotal.multiply(precioUnitario);

                response.setConcepto(concepto);
                response.setMontoContractual(montoContractual);

                // Calculate missing values
                java.math.BigDecimal cantidadAcumuladaAnterior = item.getAcumuladoAnterior();
                java.math.BigDecimal montoAnterior = cantidadAcumuladaAnterior.multiply(precioUnitario);

                java.math.BigDecimal cantidadActual = item.getCantidadAvance();
                java.math.BigDecimal montoActual = item.getImporte();

                java.math.BigDecimal cantidadAcumulada = cantidadAcumuladaAnterior.add(cantidadActual);
                java.math.BigDecimal montoAcumulado = montoAnterior.add(montoActual);

                // Percentages
                java.math.BigDecimal pctAnterior = metradoTotal.compareTo(java.math.BigDecimal.ZERO) > 0
                                ? cantidadAcumuladaAnterior.divide(metradoTotal, 4, java.math.RoundingMode.HALF_UP)
                                                .multiply(new java.math.BigDecimal("100"))
                                : java.math.BigDecimal.ZERO;

                java.math.BigDecimal pctActual = metradoTotal.compareTo(java.math.BigDecimal.ZERO) > 0
                                ? cantidadActual.divide(metradoTotal, 4, java.math.RoundingMode.HALF_UP).multiply(
                                                new java.math.BigDecimal("100"))
                                : java.math.BigDecimal.ZERO;

                java.math.BigDecimal pctAcumulado = metradoTotal.compareTo(java.math.BigDecimal.ZERO) > 0
                                ? cantidadAcumulada.divide(metradoTotal, 4, java.math.RoundingMode.HALF_UP)
                                                .multiply(new java.math.BigDecimal("100"))
                                : java.math.BigDecimal.ZERO;

                response.setPorcentajeAnterior(pctAnterior);
                response.setMontoAnterior(montoAnterior);
                response.setPorcentajeActual(pctActual);
                response.setMontoActual(montoActual);
                response.setPorcentajeAcumulado(pctAcumulado);
                response.setMontoAcumulado(montoAcumulado);

                response.setSaldoPorEjercer(montoContractual.subtract(montoAcumulado));

                return response;
        }
}
