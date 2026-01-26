package com.budgetpro.infrastructure.persistence.converter;

import com.budgetpro.domain.proyecto.model.EstadoProyecto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte estados legacy de Proyecto a estados canónicos y viceversa.
 * Mantiene compatibilidad técnica con BD sin exponer legacy al dominio.
 */
@Converter(autoApply = false)
public class EstadoProyectoConverter implements AttributeConverter<EstadoProyecto, String> {

    @Override
    public String convertToDatabaseColumn(EstadoProyecto attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public EstadoProyecto convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        String value = dbData.trim().toUpperCase();
        return switch (value) {
            case "BORRADOR" -> EstadoProyecto.BORRADOR;
            case "ACTIVO", "EJECUCION" -> EstadoProyecto.ACTIVO;
            case "SUSPENDIDO", "PAUSADO" -> EstadoProyecto.SUSPENDIDO;
            case "CERRADO", "FINALIZADO" -> EstadoProyecto.CERRADO;
            default -> throw new IllegalArgumentException("Estado de proyecto no reconocido: " + dbData);
        };
    }
}
