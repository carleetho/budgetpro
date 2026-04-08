package com.budgetpro.infrastructure.persistence.adapter.rrhh;

import com.budgetpro.application.rrhh.port.out.ConfiguracionLaboralRepositoryPort;
import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral;
import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboralId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.model.ConfiguracionLaboralExtendida;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.ConfiguracionLaboralExtendidaEntity;
import com.budgetpro.infrastructure.persistence.mapper.rrhh.ConfiguracionLaboralExtendidaMapper;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.rrhh.ConfiguracionLaboralExtendidaJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("rrhhConfiguracionLaboralRepositoryAdapter")
public class ConfiguracionLaboralRepositoryAdapter implements ConfiguracionLaboralRepositoryPort {

    private final ConfiguracionLaboralExtendidaJpaRepository repository;
    private final ConfiguracionLaboralExtendidaMapper extendidaMapper;
    private final ProyectoJpaRepository proyectoJpaRepository;

    public ConfiguracionLaboralRepositoryAdapter(ConfiguracionLaboralExtendidaJpaRepository repository,
            ConfiguracionLaboralExtendidaMapper extendidaMapper, ProyectoJpaRepository proyectoJpaRepository) {
        this.repository = repository;
        this.extendidaMapper = extendidaMapper;
        this.proyectoJpaRepository = proyectoJpaRepository;
    }

    @Override
    public Optional<ConfiguracionLaboral> findEffectiveConfig(ProyectoId proyectoId, LocalDate fecha) {
        // 1. Try project-specific config
        var projectConfig = repository
                .findFirstByProyecto_IdAndFechaVigenciaInicioLessThanEqualOrderByFechaVigenciaInicioDesc(
                        proyectoId.getValue(), fecha);

        if (projectConfig.isPresent()) {
            return projectConfig.map(this::toSobrecostoDomain);
        }

        // 2. Fallback to global config
        var globalConfig = repository
                .findFirstByProyectoIsNullAndFechaVigenciaInicioLessThanEqualOrderByFechaVigenciaInicioDesc(fecha);

        return globalConfig.map(this::toSobrecostoDomain);
    }

    private ConfiguracionLaboral toSobrecostoDomain(ConfiguracionLaboralExtendidaEntity entity) {
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
    public ConfiguracionLaboralExtendida save(ConfiguracionLaboralExtendida config) {
        UUID id = UUID.fromString(config.getId());
        ConfiguracionLaboralExtendidaEntity entity = repository.findById(id).orElseGet(() -> {
            ConfiguracionLaboralExtendidaEntity e = new ConfiguracionLaboralExtendidaEntity();
            e.setId(id);
            return e;
        });
        ProyectoEntity proyecto = null;
        if (config.getProyectoId() != null) {
            proyecto = proyectoJpaRepository.getReferenceById(config.getProyectoId().getValue());
        }
        extendidaMapper.copyToEntity(config, proyecto, entity);
        return extendidaMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<ConfiguracionLaboralExtendida> findActiveByProyecto(ProyectoId proyectoId) {
        return repository.findByProyecto_IdAndFechaVigenciaFinIsNull(proyectoId.getValue()).map(extendidaMapper::toDomain);
    }

    @Override
    public Optional<ConfiguracionLaboralExtendida> findGlobalActive() {
        return repository.findByProyectoIsNullAndFechaVigenciaFinIsNull().map(extendidaMapper::toDomain);
    }

    @Override
    public List<ConfiguracionLaboralExtendida> findHistoryByProyecto(ProyectoId proyectoId, LocalDate start,
            LocalDate end) {
        return repository
                .findByProyecto_IdAndFechaVigenciaInicioBetweenOrderByFechaVigenciaInicioAsc(proyectoId.getValue(), start,
                        end)
                .stream().map(extendidaMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ConfiguracionLaboralExtendida> findHistoryGlobal(LocalDate start, LocalDate end) {
        return repository.findByProyectoIsNullAndFechaVigenciaInicioBetweenOrderByFechaVigenciaInicioAsc(start, end)
                .stream().map(extendidaMapper::toDomain).collect(Collectors.toList());
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
