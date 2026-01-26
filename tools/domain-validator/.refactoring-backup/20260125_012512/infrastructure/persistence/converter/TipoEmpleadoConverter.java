package com.budgetpro.infrastructure.persistence.converter;

import com.budgetpro.domain.rrhh.model.TipoEmpleado;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoEmpleadoConverter implements AttributeConverter<TipoEmpleado, String> {

    @Override
    public String convertToDatabaseColumn(TipoEmpleado attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public TipoEmpleado convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return TipoEmpleado.valueOf(dbData);
    }
}
