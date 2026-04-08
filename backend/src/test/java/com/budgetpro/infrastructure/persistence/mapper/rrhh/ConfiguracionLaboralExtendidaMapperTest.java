package com.budgetpro.infrastructure.persistence.mapper.rrhh;

import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.model.ConfiguracionLaboralExtendida;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.ConfiguracionLaboralExtendidaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConfiguracionLaboralExtendidaMapperTest {

    private final ConfiguracionLaboralExtendidaMapper mapper = new ConfiguracionLaboralExtendidaMapper();

    @Test
    void toDomain_deserializaNueveClavesJsonb() {
        UUID id = UUID.randomUUID();
        UUID proyectoUuid = UUID.randomUUID();
        ProyectoEntity proyecto = new ProyectoEntity();
        proyecto.setId(proyectoUuid);

        Map<String, Object> fsr = new HashMap<>();
        fsr.put("diasAguinaldo", 15);
        fsr.put("diasVacaciones", 12);
        fsr.put("porcentajeSeguridadSocial", new BigDecimal("9.25"));
        fsr.put("diasNoTrabajados", 4);
        fsr.put("diasLaborablesAno", 250);
        fsr.put("factorHorasExtras", new BigDecimal("0.25"));
        fsr.put("factorTurnoNocturno", new BigDecimal("0.10"));
        fsr.put("factorRiesgo", new BigDecimal("0.05"));
        fsr.put("factorRegional", new BigDecimal("0.02"));

        ConfiguracionLaboralExtendidaEntity entity = new ConfiguracionLaboralExtendidaEntity();
        entity.setId(id);
        entity.setProyecto(proyecto);
        entity.setFechaVigenciaInicio(LocalDate.of(2025, 1, 1));
        entity.setFechaVigenciaFin(null);
        entity.setFsrConfig(fsr);

        ConfiguracionLaboralExtendida domain = mapper.toDomain(entity);

        assertEquals(id.toString(), domain.getId());
        assertEquals(ProyectoId.from(proyectoUuid), domain.getProyectoId());
        assertEquals(15, domain.getDiasAguinaldo());
        assertEquals(12, domain.getDiasVacaciones());
        assertEquals(new BigDecimal("9.25"), domain.getPorcentajeSeguridadSocial());
        assertEquals(4, domain.getDiasNoTrabajados());
        assertEquals(250, domain.getDiasLaborablesAno());
        assertEquals(new BigDecimal("0.25"), domain.getFactorHorasExtras());
        assertEquals(new BigDecimal("0.10"), domain.getFactorTurnoNocturno());
        assertEquals(new BigDecimal("0.05"), domain.getFactorRiesgo());
        assertEquals(new BigDecimal("0.02"), domain.getFactorRegional());
    }

    @Test
    void copyToEntity_incluyeLasNueveClavesEnFsrConfig() {
        UUID id = UUID.randomUUID();
        ProyectoEntity proyecto = new ProyectoEntity();
        proyecto.setId(UUID.randomUUID());

        ConfiguracionLaboralExtendida domain = new ConfiguracionLaboralExtendida(id.toString(), ProyectoId.from(proyecto.getId()),
                LocalDate.of(2025, 6, 1), null, 14, 14, new BigDecimal("30"), 5, 251, new BigDecimal("0.1"),
                new BigDecimal("0.2"), new BigDecimal("0.3"), new BigDecimal("0.4"));

        ConfiguracionLaboralExtendidaEntity entity = new ConfiguracionLaboralExtendidaEntity();
        mapper.copyToEntity(domain, proyecto, entity);

        Map<String, Object> fsr = entity.getFsrConfig();
        assertEquals(14, fsr.get("diasAguinaldo"));
        assertEquals(14, fsr.get("diasVacaciones"));
        assertEquals(new BigDecimal("30"), fsr.get("porcentajeSeguridadSocial"));
        assertEquals(5, fsr.get("diasNoTrabajados"));
        assertEquals(251, fsr.get("diasLaborablesAno"));
        assertEquals(new BigDecimal("0.1"), fsr.get("factorHorasExtras"));
        assertEquals(new BigDecimal("0.2"), fsr.get("factorTurnoNocturno"));
        assertEquals(new BigDecimal("0.3"), fsr.get("factorRiesgo"));
        assertEquals(new BigDecimal("0.4"), fsr.get("factorRegional"));
    }

    @Test
    void toDomain_configGlobal_proyectoIdNulo() {
        ConfiguracionLaboralExtendidaEntity entity = new ConfiguracionLaboralExtendidaEntity();
        entity.setId(UUID.randomUUID());
        entity.setProyecto(null);
        entity.setFechaVigenciaInicio(LocalDate.of(2025, 1, 1));
        entity.setFechaVigenciaFin(null);
        entity.setFsrConfig(Map.of("diasAguinaldo", 10, "diasVacaciones", 10, "porcentajeSeguridadSocial",
                BigDecimal.ZERO, "diasNoTrabajados", 0, "diasLaborablesAno", 251));

        ConfiguracionLaboralExtendida domain = mapper.toDomain(entity);
        assertNull(domain.getProyectoId());
    }
}
