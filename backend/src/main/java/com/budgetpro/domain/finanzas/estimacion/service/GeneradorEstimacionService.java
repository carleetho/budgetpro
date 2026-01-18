package com.budgetpro.domain.finanzas.estimacion.service;

import com.budgetpro.domain.finanzas.estimacion.model.DetalleEstimacion;
import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de Dominio para generar y calcular estimaciones de avance.
 * 
 * Implementa la metodología de Suárez Salazar (Cap. 1.3520 - Gráfica de Ingresos).
 * 
 * Responsabilidad:
 * - Calcular amortización de anticipo
 * - Calcular retención de fondo de garantía
 * - Validar volúmenes (no permitir estimar más del 100%)
 * - Calcular montos (bruto, neto)
 * 
 * No persiste, solo calcula.
 */
public class GeneradorEstimacionService {

    /**
     * Calcula la amortización de anticipo.
     * 
     * Si hubo un anticipo del X%, cada estimación debe amortizar el X% de su monto bruto
     * hasta agotar el saldo del anticipo.
     * 
     * @param montoBruto Monto bruto de la estimación
     * @param porcentajeAnticipo Porcentaje de anticipo (ej: 30%)
     * @param saldoAnticipoPendiente Saldo pendiente de amortizar del anticipo
     * @return Monto a amortizar (no puede exceder el saldo pendiente)
     */
    public BigDecimal calcularAmortizacionAnticipo(BigDecimal montoBruto, BigDecimal porcentajeAnticipo,
                                                   BigDecimal saldoAnticipoPendiente) {
        if (montoBruto == null || montoBruto.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (porcentajeAnticipo == null || porcentajeAnticipo.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (saldoAnticipoPendiente == null || saldoAnticipoPendiente.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Calcular amortización teórica: montoBruto * porcentajeAnticipo
        BigDecimal amortizacionTeorica = montoBruto.multiply(porcentajeAnticipo)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        // No puede exceder el saldo pendiente
        return amortizacionTeorica.min(saldoAnticipoPendiente);
    }

    /**
     * Calcula la retención de fondo de garantía.
     * 
     * @param montoBruto Monto bruto de la estimación
     * @param porcentajeRetencion Porcentaje de retención (ej: 5%)
     * @return Monto retenido
     */
    public BigDecimal calcularRetencionFondoGarantia(BigDecimal montoBruto, BigDecimal porcentajeRetencion) {
        if (montoBruto == null || montoBruto.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (porcentajeRetencion == null || porcentajeRetencion.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return montoBruto.multiply(porcentajeRetencion)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
    }

    /**
     * Valida que no se estime más volumen del contratado.
     * 
     * @param cantidadAvance Cantidad a estimar en este periodo
     * @param acumuladoAnterior Acumulado de estimaciones anteriores
     * @param volumenContratado Volumen total contratado de la partida
     * @return true si la estimación es válida (no excede el 100%), false en caso contrario
     */
    public boolean validarVolumenEstimado(BigDecimal cantidadAvance, BigDecimal acumuladoAnterior,
                                         BigDecimal volumenContratado) {
        if (cantidadAvance == null || cantidadAvance.compareTo(BigDecimal.ZERO) <= 0) {
            return true; // Si no hay avance, no hay problema
        }
        if (volumenContratado == null || volumenContratado.compareTo(BigDecimal.ZERO) <= 0) {
            return false; // No se puede estimar si no hay volumen contratado
        }

        BigDecimal acumuladoAnteriorFinal = acumuladoAnterior != null ? acumuladoAnterior : BigDecimal.ZERO;
        BigDecimal acumuladoTotal = acumuladoAnteriorFinal.add(cantidadAvance);

        // El acumulado total no puede exceder el volumen contratado
        return acumuladoTotal.compareTo(volumenContratado) <= 0;
    }

    /**
     * Calcula el acumulado anterior de una partida basándose en estimaciones previas.
     * 
     * @param partidaId ID de la partida
     * @param estimacionesPrevias Lista de estimaciones previas (ya aprobadas)
     * @return Acumulado anterior de la partida
     */
    public BigDecimal calcularAcumuladoAnterior(UUID partidaId, List<Estimacion> estimacionesPrevias) {
        if (estimacionesPrevias == null || estimacionesPrevias.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return estimacionesPrevias.stream()
                .flatMap(estimacion -> estimacion.getDetalles().stream())
                .filter(detalle -> detalle.getPartidaId().equals(partidaId))
                .map(DetalleEstimacion::getCantidadAvance)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el monto neto a pagar.
     * 
     * Fórmula: montoBruto - amortizacionAnticipo - retencionFondoGarantia
     * 
     * @param montoBruto Monto bruto
     * @param amortizacionAnticipo Amortización de anticipo
     * @param retencionFondoGarantia Retención de fondo de garantía
     * @return Monto neto a pagar
     */
    public BigDecimal calcularMontoNetoPagar(BigDecimal montoBruto, BigDecimal amortizacionAnticipo,
                                             BigDecimal retencionFondoGarantia) {
        BigDecimal montoBrutoFinal = montoBruto != null ? montoBruto : BigDecimal.ZERO;
        BigDecimal amortizacionFinal = amortizacionAnticipo != null ? amortizacionAnticipo : BigDecimal.ZERO;
        BigDecimal retencionFinal = retencionFondoGarantia != null ? retencionFondoGarantia : BigDecimal.ZERO;

        return montoBrutoFinal
                .subtract(amortizacionFinal)
                .subtract(retencionFinal)
                .setScale(4, RoundingMode.HALF_UP);
    }
}
