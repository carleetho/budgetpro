package com.budgetpro.domain.logistica.transferencia.event;

import com.budgetpro.domain.logistica.transferencia.model.TransferenciaId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de dominio emitido cuando se transfiere material entre proyectos.
 * Utilizado por el módulo de Finanzas para generar deuda/CXC/CXP.
 */
public record MaterialTransferredBetweenProjects(
        TransferenciaId transferenciaId,
        UUID proyectoOrigenId,
        UUID proyectoDestinoId,
        String recursoExternalId,
        BigDecimal cantidad,
        BigDecimal valorPMP, // Costo total: cantidad * PMP_origen
        LocalDateTime fechaTransferencia) {
}
