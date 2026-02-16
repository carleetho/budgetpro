package com.budgetpro.domain.logistica.compra.port.out;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Puerto de salida para validación de presupuesto.
 * 
 * Valida disponibilidad de presupuesto según regla L-01.
 */
public interface PresupuestoValidator {

    /**
     * Valida que el monto total no exceda el presupuesto disponible del proyecto.
     * 
     * @param proyectoId ID del proyecto
     * @param montoTotal Monto total a validar
     * @throws IllegalStateException si el monto excede el presupuesto disponible
     */
    void validarDisponibilidadPresupuesto(UUID proyectoId, BigDecimal montoTotal);
}
