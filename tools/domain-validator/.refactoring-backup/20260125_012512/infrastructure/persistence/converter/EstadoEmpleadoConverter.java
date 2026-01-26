package com.budgetpro.infrastructure.persistence.converter;

import com.budgetpro.domain.rrhh.model.EstadoEmpleado;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoEmpleadoConverter implements AttributeConverter<EstadoEmpleado, String> {

    @Override
    public String convertToDatabaseColumn(EstadoEmpleado attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public EstadoEmpleado convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return EstadoEmpleado.valueOf(dbData);
    }
}
