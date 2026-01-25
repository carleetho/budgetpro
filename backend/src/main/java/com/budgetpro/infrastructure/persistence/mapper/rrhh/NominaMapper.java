package com.budgetpro.infrastructure.persistence.mapper.rrhh;

import com.budgetpro.domain.rrhh.model.Nomina;
import com.budgetpro.domain.rrhh.model.NominaId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.infrastructure.persistence.entity.rrhh.NominaEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import org.springframework.stereotype.Component;

@Component
public class NominaMapper {

    public Nomina toDomain(NominaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Nomina.reconstruir(NominaId.of(entity.getId()), ProyectoId.from(entity.getProyecto().getId()),
                entity.getPeriodoInicio(), entity.getPeriodoFin(), entity.getDescripcion(), entity.getEstado(),
                entity.getTotalBruto(), entity.getTotalNeto(), entity.getCantidadEmpleados(), java.util.List.of());
    }

    public NominaEntity toEntity(Nomina domain) {
        if (domain == null) {
            return null;
        }
        NominaEntity entity = new NominaEntity();
        entity.setId(domain.getId().getValue());

        ProyectoEntity proyecto = new ProyectoEntity();
        proyecto.setId(domain.getProyectoId().getValue());
        entity.setProyecto(proyecto);

        entity.setPeriodoInicio(domain.getPeriodoInicio());
        entity.setPeriodoFin(domain.getPeriodoFin());
        entity.setDescripcion(domain.getDescripcion());
        entity.setEstado(domain.getEstado());
        entity.setTotalBruto(domain.getTotalBruto());
        entity.setTotalNeto(domain.getTotalNeto());
        entity.setCantidadEmpleados(domain.getCantidadEmpleados());
        return entity;
    }
}
