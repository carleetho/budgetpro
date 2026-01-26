package com.budgetpro.infrastructure.persistence.converter;

import com.budgetpro.domain.rrhh.model.EstadoAsistencia;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoAsistenciaConverter implements AttributeConverter<EstadoAsistencia, String> {

    @Override
    public String convertToDatabaseColumn(EstadoAsistencia attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public EstadoAsistencia convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return EstadoAsistencia.valueOf(dbData);
    }
}
