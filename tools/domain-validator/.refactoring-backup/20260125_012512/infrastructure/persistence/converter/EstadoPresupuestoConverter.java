package com.budgetpro.infrastructure.persistence.converter;

import com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte estados legacy de Presupuesto a estados canónicos y viceversa.
 * Mantiene compatibilidad técnica con BD sin exponer legacy al dominio.
 */
@Converter(autoApply = false)
public class EstadoPresupuestoConverter implements AttributeConverter<EstadoPresupuesto, String> {

    @Override
    public String convertToDatabaseColumn(EstadoPresupuesto attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public EstadoPresupuesto convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        String value = dbData.trim().toUpperCase();
        return switch (value) {
            case "BORRADOR", "EN_EDICION" -> EstadoPresupuesto.BORRADOR;
            case "CONGELADO", "APROBADO" -> EstadoPresupuesto.CONGELADO;
            case "INVALIDADO", "ANULADO" -> EstadoPresupuesto.INVALIDADO;
            default -> throw new IllegalArgumentException("Estado de presupuesto no reconocido: " + dbData);
        };
    }
}
