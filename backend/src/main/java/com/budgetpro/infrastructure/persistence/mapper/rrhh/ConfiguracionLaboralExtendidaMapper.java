package com.budgetpro.infrastructure.persistence.mapper.rrhh;

import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.model.ConfiguracionLaboralExtendida;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.ConfiguracionLaboralExtendidaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Maps {@link ConfiguracionLaboralExtendida} domain type to/from
 * {@link ConfiguracionLaboralExtendidaEntity}, including FSR fields in
 * {@code fsr_config} JSONB.
 */
@Component
public class ConfiguracionLaboralExtendidaMapper {

    public ConfiguracionLaboralExtendida toDomain(ConfiguracionLaboralExtendidaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ConfiguracionLaboralExtendidaEntity must not be null");
        }
        Map<String, Object> fsr = entity.getFsrConfig();
        if (fsr == null) {
            fsr = Map.of();
        }
        ProyectoId proyectoId = entity.getProyecto() != null ? ProyectoId.from(entity.getProyecto().getId()) : null;
        return new ConfiguracionLaboralExtendida(entity.getId().toString(), proyectoId, entity.getFechaVigenciaInicio(),
                entity.getFechaVigenciaFin(), getInteger(fsr, "diasAguinaldo"), getInteger(fsr, "diasVacaciones"),
                getBigDecimal(fsr, "porcentajeSeguridadSocial"), getInteger(fsr, "diasNoTrabajados"),
                getInteger(fsr, "diasLaborablesAno"), getBigDecimal(fsr, "factorHorasExtras"),
                getBigDecimal(fsr, "factorTurnoNocturno"), getBigDecimal(fsr, "factorRiesgo"),
                getBigDecimal(fsr, "factorRegional"));
    }

    /**
     * Copies domain state onto the target entity (insert or update).
     */
    public void copyToEntity(ConfiguracionLaboralExtendida domain, ProyectoEntity proyectoOrNull,
            ConfiguracionLaboralExtendidaEntity target) {
        if (domain == null) {
            throw new IllegalArgumentException("ConfiguracionLaboralExtendida domain must not be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("ConfiguracionLaboralExtendidaEntity target must not be null");
        }
        target.setId(UUID.fromString(domain.getId()));
        target.setProyecto(proyectoOrNull);
        target.setFechaVigenciaInicio(domain.getFechaInicio());
        target.setFechaVigenciaFin(domain.getFechaFin());
        target.setFsrConfig(packFsrConfig(domain));
    }

    private Map<String, Object> packFsrConfig(ConfiguracionLaboralExtendida domain) {
        Map<String, Object> map = new HashMap<>();
        map.put("diasAguinaldo", domain.getDiasAguinaldo());
        map.put("diasVacaciones", domain.getDiasVacaciones());
        map.put("porcentajeSeguridadSocial", domain.getPorcentajeSeguridadSocial());
        map.put("diasNoTrabajados", domain.getDiasNoTrabajados());
        map.put("diasLaborablesAno", domain.getDiasLaborablesAno());
        map.put("factorHorasExtras", domain.getFactorHorasExtras());
        map.put("factorTurnoNocturno", domain.getFactorTurnoNocturno());
        map.put("factorRiesgo", domain.getFactorRiesgo());
        map.put("factorRegional", domain.getFactorRegional());
        return map;
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return 0;
    }

    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return new BigDecimal(val.toString());
        }
        if (val instanceof String) {
            try {
                return new BigDecimal((String) val);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
}
