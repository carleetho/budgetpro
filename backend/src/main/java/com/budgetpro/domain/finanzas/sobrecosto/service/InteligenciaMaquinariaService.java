package com.budgetpro.domain.finanzas.sobrecosto.service;

import com.budgetpro.domain.finanzas.apu.model.APU;
import com.budgetpro.domain.finanzas.apu.model.ApuInsumo;
import com.budgetpro.domain.recurso.model.TipoRecurso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de Dominio para generar alertas de inteligencia de negocio relacionadas con maquinaria.
 * 
 * Basado en metodología de Suárez Salazar (Pág. 174) sobre Depreciación y Mantenimiento.
 * 
 * Responsabilidad:
 * - Detectar maquinaria propia (costo $0) y alertar sobre costos de posesión
 * - Validar rendimientos fuera de estándar
 * 
 * No persiste, solo genera alertas.
 */
public class InteligenciaMaquinariaService {

    /**
     * Analiza un APU y genera alertas de inteligencia de negocio.
     * 
     * @param apu El APU a analizar
     * @return Lista de alertas generadas
     */
    public List<AlertaInteligencia> analizarAPU(APU apu) {
        List<AlertaInteligencia> alertas = new ArrayList<>();

        // Analizar cada insumo del APU
        for (ApuInsumo insumo : apu.getInsumos()) {
            // Alerta de Equipo Propio (costo $0)
            if (insumo.getPrecioUnitario().compareTo(BigDecimal.ZERO) == 0) {
                // Nota: Necesitaríamos el Recurso para verificar el tipo, pero por ahora
                // asumimos que si el precio es $0, podría ser maquinaria propia
                alertas.add(new AlertaInteligencia(
                    TipoAlerta.DESCAPITALIZACION_MAQUINARIA,
                    String.format("Alerta de Descapitalización: El insumo con recursoId %s tiene costo $0.00. " +
                                 "Maquinaria propia requiere costos de Depreciación y Mantenimiento " +
                                 "(Ver metodología Suárez Salazar Pág. 174). " +
                                 "Considere usar el método calcularCostoHorarioPosesion() para calcular " +
                                 "el costo real de posesión de la maquinaria.",
                                 insumo.getRecursoId()),
                    insumo.getRecursoId()
                ));
            }
        }

        return alertas;
    }

    /**
     * Calcula el costo horario de posesión de maquinaria según metodología Suárez Salazar (Pág. 174).
     * 
     * Fórmula simplificada del libro:
     * CostoHorario = (Depreciación + Mantenimiento + Seguros + Almacenaje) / HorasAnuales
     * 
     * @param valorInicial Valor inicial de la maquinaria
     * @param valorResidual Valor residual (al final de vida útil)
     * @param vidaUtilAnos Vida útil en años
     * @param horasAnualesUso Horas anuales de uso
     * @param porcentajeMantenimiento % de mantenimiento sobre valor inicial
     * @param porcentajeSeguros % de seguros sobre valor inicial
     * @param porcentajeAlmacenaje % de almacenaje sobre valor inicial
     * @return Costo horario de posesión
     */
    public BigDecimal calcularCostoHorarioPosesion(BigDecimal valorInicial,
                                                   BigDecimal valorResidual,
                                                   Integer vidaUtilAnos,
                                                   Integer horasAnualesUso,
                                                   BigDecimal porcentajeMantenimiento,
                                                   BigDecimal porcentajeSeguros,
                                                   BigDecimal porcentajeAlmacenaje) {
        if (valorInicial == null || valorInicial.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor inicial debe ser positivo");
        }
        if (vidaUtilAnos == null || vidaUtilAnos <= 0) {
            throw new IllegalArgumentException("La vida útil debe ser positiva");
        }
        if (horasAnualesUso == null || horasAnualesUso <= 0) {
            throw new IllegalArgumentException("Las horas anuales de uso deben ser positivas");
        }

        // Depreciación anual = (ValorInicial - ValorResidual) / VidaUtilAnos
        BigDecimal valorResidualFinal = valorResidual != null ? valorResidual : BigDecimal.ZERO;
        BigDecimal depreciacionAnual = valorInicial.subtract(valorResidualFinal)
                .divide(new BigDecimal(vidaUtilAnos), 4, RoundingMode.HALF_UP);

        // Mantenimiento anual = ValorInicial × %Mantenimiento
        BigDecimal porcentajeMant = porcentajeMantenimiento != null ? porcentajeMantenimiento : BigDecimal.ZERO;
        BigDecimal mantenimientoAnual = valorInicial.multiply(porcentajeMant)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        // Seguros anual = ValorInicial × %Seguros
        BigDecimal porcentajeSeg = porcentajeSeguros != null ? porcentajeSeguros : BigDecimal.ZERO;
        BigDecimal segurosAnual = valorInicial.multiply(porcentajeSeg)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        // Almacenaje anual = ValorInicial × %Almacenaje
        BigDecimal porcentajeAlm = porcentajeAlmacenaje != null ? porcentajeAlmacenaje : BigDecimal.ZERO;
        BigDecimal almacenajeAnual = valorInicial.multiply(porcentajeAlm)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        // Costo total anual = Depreciación + Mantenimiento + Seguros + Almacenaje
        BigDecimal costoTotalAnual = depreciacionAnual
                .add(mantenimientoAnual)
                .add(segurosAnual)
                .add(almacenajeAnual);

        // Costo horario = CostoTotalAnual / HorasAnualesUso
        return costoTotalAnual.divide(new BigDecimal(horasAnualesUso), 4, RoundingMode.HALF_UP);
    }

    /**
     * Valida si un rendimiento difiere significativamente del estándar paramétrico.
     * 
     * @param rendimientoActual Rendimiento actual del APU
     * @param rendimientoEstandar Rendimiento estándar paramétrico
     * @param umbralPorcentaje Umbral de diferencia (ej: 20%)
     * @return true si la diferencia es mayor al umbral
     */
    public boolean validarRendimiento(BigDecimal rendimientoActual, BigDecimal rendimientoEstandar,
                                      BigDecimal umbralPorcentaje) {
        if (rendimientoActual == null || rendimientoEstandar == null || umbralPorcentaje == null) {
            return false;
        }

        if (rendimientoEstandar.compareTo(BigDecimal.ZERO) == 0) {
            return false; // No se puede comparar si el estándar es cero
        }

        // Calcular diferencia porcentual
        BigDecimal diferencia = rendimientoActual.subtract(rendimientoEstandar)
                .abs()
                .divide(rendimientoEstandar, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return diferencia.compareTo(umbralPorcentaje) > 0;
    }

    /**
     * Enum que representa los tipos de alertas de inteligencia.
     */
    public enum TipoAlerta {
        DESCAPITALIZACION_MAQUINARIA,
        RENDIMIENTO_ATIPICO
    }

    /**
     * Clase que representa una alerta de inteligencia de negocio.
     */
    public static class AlertaInteligencia {
        private final TipoAlerta tipo;
        private final String mensaje;
        private final java.util.UUID recursoId; // Opcional: ID del recurso relacionado

        public AlertaInteligencia(TipoAlerta tipo, String mensaje, java.util.UUID recursoId) {
            this.tipo = tipo;
            this.mensaje = mensaje;
            this.recursoId = recursoId;
        }

        public TipoAlerta getTipo() {
            return tipo;
        }

        public String getMensaje() {
            return mensaje;
        }

        public java.util.UUID getRecursoId() {
            return recursoId;
        }
    }
}
