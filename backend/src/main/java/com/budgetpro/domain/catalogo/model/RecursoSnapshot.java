package com.budgetpro.domain.catalogo.model;

import com.budgetpro.domain.recurso.model.TipoRecurso;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Value Object que representa un recurso obtenido desde un catálogo externo.
 */
public record RecursoSnapshot(
        String externalId,
        String catalogSource,
        String nombre,
        TipoRecurso tipo,
        String unidad,
        BigDecimal precioReferencial,
        LocalDateTime fetchedAt
) {
    public RecursoSnapshot {
        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("El externalId no puede estar vacío");
        }
        if (catalogSource == null || catalogSource.isBlank()) {
            throw new IllegalArgumentException("El catalogSource no puede estar vacío");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo no puede ser nulo");
        }
        if (unidad == null || unidad.isBlank()) {
            throw new IllegalArgumentException("La unidad no puede estar vacía");
        }
        if (precioReferencial == null || precioReferencial.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precioReferencial no puede ser nulo ni negativo");
        }
        if (fetchedAt == null) {
            throw new IllegalArgumentException("El fetchedAt no puede ser nulo");
        }
    }
}
