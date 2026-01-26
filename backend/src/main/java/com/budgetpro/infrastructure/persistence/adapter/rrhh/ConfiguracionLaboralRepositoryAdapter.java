package com.budgetpro.infrastructure.persistence.adapter.rrhh;

import com.budgetpro.application.rrhh.port.out.ConfiguracionLaboralRepositoryPort;
import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral;
import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboralId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.infrastructure.persistence.entity.rrhh.ConfiguracionLaboralExtendidaEntity;
import com.budgetpro.infrastructure.persistence.repository.rrhh.ConfiguracionLaboralExtendidaJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component("rrhhConfiguracionLaboralRepositoryAdapter")
public class ConfiguracionLaboralRepositoryAdapter implements ConfiguracionLaboralRepositoryPort {

    private final ConfiguracionLaboralExtendidaJpaRepository repository;

    public ConfiguracionLaboralRepositoryAdapter(ConfiguracionLaboralExtendidaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ConfiguracionLaboral> findEffectiveConfig(ProyectoId proyectoId, LocalDate fecha) {
        // 1. Try project-specific config
        var projectConfig = repository
                .findFirstByProyectoIdAndFechaVigenciaInicioLessThanEqualOrderByFechaVigenciaInicioDesc(
                        proyectoId.getValue(), fecha);

        if (projectConfig.isPresent()) {
            return projectConfig.map(this::toDomain);
        }

        // 2. Fallback to global config
        var globalConfig = repository
                .findFirstByProyectoIdIsNullAndFechaVigenciaInicioLessThanEqualOrderByFechaVigenciaInicioDesc(fecha);

        return globalConfig.map(this::toDomain);
    }

    private ConfiguracionLaboral toDomain(ConfiguracionLaboralExtendidaEntity entity) {
        if (entity == null)
            return null;

        Map<String, Object> fsrConfig = entity.getFsrConfig();
        if (fsrConfig == null) {
            fsrConfig = Map.of();
        }

        // Extract fields from JSON map
        Integer diasAguinaldo = getInteger(fsrConfig, "diasAguinaldo");
        Integer diasVacaciones = getInteger(fsrConfig, "diasVacaciones");
        BigDecimal porcentajeSeguridadSocial = getBigDecimal(fsrConfig, "porcentajeSeguridadSocial");
        Integer diasNoTrabajados = getInteger(fsrConfig, "diasNoTrabajados");
        Integer diasLaborablesAno = getInteger(fsrConfig, "diasLaborablesAno");

        UUID proyectoId = entity.getProyecto() != null ? entity.getProyecto().getId() : null;

        return ConfiguracionLaboral.reconstruir(ConfiguracionLaboralId.of(entity.getId()), proyectoId, diasAguinaldo,
                diasVacaciones, porcentajeSeguridadSocial, diasNoTrabajados, diasLaborablesAno, 0L // Version handling
                                                                                                   // might differ in
                                                                                                   // this extendida
                                                                                                   // entity, creating
                                                                                                   // as 0
        );
    }

    @Override
    public com.budgetpro.domain.rrhh.model.ConfiguracionLaboralExtendida save(
            com.budgetpro.domain.rrhh.model.ConfiguracionLaboralExtendida config) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<com.budgetpro.domain.rrhh.model.ConfiguracionLaboralExtendida> findActiveByProyecto(
            ProyectoId proyectoId) {
        return Optional.empty();
    }

    @Override
    public Optional<com.budgetpro.domain.rrhh.model.ConfiguracionLaboralExtendida> findGlobalActive() {
        return Optional.empty();
    }

    @Override
    public java.util.List<com.budgetpro.domain.rrhh.model.ConfiguracionLaboralExtendida> findHistoryByProyecto(
            ProyectoId proyectoId, LocalDate start, LocalDate end) {
        return java.util.List.of();
    }

    @Override
    public java.util.List<com.budgetpro.domain.rrhh.model.ConfiguracionLaboralExtendida> findHistoryGlobal(
            LocalDate start, LocalDate end) {
        return java.util.List.of();
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return 0; // Default
    }

    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) {
            // Handle Double/Integer to BigDecimal
            return new BigDecimal(val.toString());
        }
        if (val instanceof String) {
            try {
                return new BigDecimal((String) val);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO; // Default
    }
}
