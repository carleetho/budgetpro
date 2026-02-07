package com.budgetpro.domain.finanzas.avance.exception;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Excepción de dominio lanzada cuando el metrado acumulado excede el metrado
 * presupuestado de una partida.
 * 
 * Esta excepción implementa la regla de negocio E-01 (REGLA-088) que establece
 * que el avance físico no puede exceder el metrado contratado sin una orden de
 * cambio aprobada: - Metrado acumulado ≤ Metrado presupuestado - Excesos
 * requieren Change Order formal - Previene sobrecostos ocultos y desviaciones
 * presupuestarias
 * 
 * **HTTP Status:** 409 Conflict **AXIOM Enforcement:** Critical financial
 * control **Severity:** HIGH
 * 
 * **Contexto Financiero:** Permitir metrados excesivos sin control puede: -
 * Generar sobrecostos no autorizados - Violarcláusulas contractuales - Crear
 * desviaciones en EVM (Earned Value Management) - Disfazar necesidades de
 * presupuesto adicional
 * 
 * @see com.budgetpro.domain.finanzas.avance.model.AvanceFisico
 * @see com.budgetpro.domain.finanzas.partida.model.Partida#getMetrado()
 */
public class MetradoExcedidoException extends RuntimeException {

    private final UUID partidaId;
    private final BigDecimal metradoPresupuestado;
    private final BigDecimal metradoAcumulado;
    private final BigDecimal exceso;

    /**
     * Constructor para violaciones de cap de metrado.
     * 
     * @param partidaId            ID de la partida con exceso de metrado
     * @param metradoPresupuestado Metrado presupuestado (límite)
     * @param metradoAcumulado     Metrado acumulado (actual + nuevo registro)
     * @param message              Mensaje adicional descriptivo
     * @throws NullPointerException si partidaId es nulo
     */
    public MetradoExcedidoException(UUID partidaId, BigDecimal metradoPresupuestado, BigDecimal metradoAcumulado,
            String message) {
        super(formatMessage(partidaId, metradoPresupuestado, metradoAcumulado, message));
        this.partidaId = Objects.requireNonNull(partidaId, "El ID de la partida no puede ser nulo");
        this.metradoPresupuestado = metradoPresupuestado;
        this.metradoAcumulado = metradoAcumulado;
        this.exceso = metradoAcumulado != null && metradoPresupuestado != null
                ? metradoAcumulado.subtract(metradoPresupuestado)
                : BigDecimal.ZERO;
    }

    /**
     * Constructor simplificado con mensaje por defecto.
     * 
     * @param partidaId            ID de la partida
     * @param metradoPresupuestado Metrado presupuestado
     * @param metradoAcumulado     Metrado acumulado
     */
    public MetradoExcedidoException(UUID partidaId, BigDecimal metradoPresupuestado, BigDecimal metradoAcumulado) {
        this(partidaId, metradoPresupuestado, metradoAcumulado, "Metrado cap exceeded: requires Change Order approval");
    }

    /**
     * Formatea el mensaje de la excepción con contexto financiero completo.
     */
    private static String formatMessage(UUID partidaId, BigDecimal metradoPresupuestado, BigDecimal metradoAcumulado,
            String message) {
        BigDecimal exceso = metradoAcumulado != null && metradoPresupuestado != null
                ? metradoAcumulado.subtract(metradoPresupuestado)
                : BigDecimal.ZERO;

        return String.format(
                "Metrado cap violation: %s. Partida: %s, Presupuestado: %s, Acumulado: %s, Exceso: %s (requires Change Order)",
                message, partidaId, metradoPresupuestado, metradoAcumulado, exceso);
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public BigDecimal getMetradoPresupuestado() {
        return metradoPresupuestado;
    }

    public BigDecimal getMetradoAcumulado() {
        return metradoAcumulado;
    }

    public BigDecimal getExceso() {
        return exceso;
    }
}
