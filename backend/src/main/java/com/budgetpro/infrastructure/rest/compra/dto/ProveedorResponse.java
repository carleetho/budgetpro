package com.budgetpro.infrastructure.rest.compra.dto;

import com.budgetpro.domain.logistica.compra.model.ProveedorEstado;
import java.util.UUID;

/**
 * DTO de respuesta REST para información de proveedor.
 */
public record ProveedorResponse(
        UUID id,
        String razonSocial,
        String ruc,
        ProveedorEstado estado
) {
    /**
     * Constructor compacto con validación.
     */
    public ProveedorResponse {
        if (id == null) {
            throw new IllegalArgumentException("id no puede ser null");
        }
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new IllegalArgumentException("razonSocial no puede ser nula o vacía");
        }
        if (ruc == null || ruc.isBlank()) {
            throw new IllegalArgumentException("ruc no puede ser nulo o vacío");
        }
        if (estado == null) {
            throw new IllegalArgumentException("estado no puede ser null");
        }
    }
}
