package com.budgetpro.domain.finanzas.compra.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de dominio que representa que una compra ha sido registrada.
 * 
 * Este evento es inmutable y se produce cuando una compra es creada exitosamente.
 * Se utiliza para desacoplar efectos secundarios (notificaciones, integraciones, etc.)
 * de la transacción principal.
 * 
 * Características:
 * - Inmutable (record)
 * - No contiene lógica de negocio
 * - Solo datos necesarios para efectos secundarios
 * - Producido por el agregado Compra
 */
public record CompraRegistradaEvent(
        UUID compraId,
        UUID proyectoId,
        UUID presupuestoId,
        BigDecimal total,
        LocalDateTime fechaRegistro
) {
    /**
     * Constructor que valida que ningún campo sea nulo.
     */
    public CompraRegistradaEvent {
        if (compraId == null) {
            throw new IllegalArgumentException("El ID de la compra no puede ser nulo");
        }
        if (proyectoId == null) {
            throw new IllegalArgumentException("El ID del proyecto no puede ser nulo");
        }
        if (presupuestoId == null) {
            throw new IllegalArgumentException("El ID del presupuesto no puede ser nulo");
        }
        if (total == null) {
            throw new IllegalArgumentException("El total no puede ser nulo");
        }
        if (fechaRegistro == null) {
            throw new IllegalArgumentException("La fecha de registro no puede ser nula");
        }
    }
}
