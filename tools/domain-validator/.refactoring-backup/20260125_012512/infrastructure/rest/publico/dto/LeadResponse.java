package com.budgetpro.infrastructure.rest.publico.dto;

import com.budgetpro.infrastructure.persistence.entity.marketing.LeadEstado;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para solicitudes de demo.
 */
public record LeadResponse(
        UUID id,
        LeadEstado estado,
        LocalDateTime fechaSolicitud
) {
}
