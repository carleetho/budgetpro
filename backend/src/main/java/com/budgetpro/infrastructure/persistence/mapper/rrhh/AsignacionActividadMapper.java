package com.budgetpro.infrastructure.persistence.mapper.rrhh;

import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.model.AsignacionActividad;
import com.budgetpro.domain.rrhh.model.CuadrillaId;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.AsignacionActividadEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.CuadrillaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AsignacionActividadMapper {

    public AsignacionActividadEntity toEntity(AsignacionActividad domain) {
        if (domain == null) {
            throw new IllegalArgumentException("AsignacionActividad domain object cannot be null for mapping to entity.");
        }

        AsignacionActividadEntity entity = new AsignacionActividadEntity();
        
        // Map ID
        entity.setId(domain.getId());
        
        // Map Cuadrilla (Stub with ID)
        if (domain.getCuadrillaId() != null) {
            CuadrillaEntity cuadrillaStub = new CuadrillaEntity();
            cuadrillaStub.setId(domain.getCuadrillaId().getValue());
            entity.setCuadrilla(cuadrillaStub);
        }
        
        // Map Partida (Stub with ID)
        if (domain.getPartidaId() != null) {
            PartidaEntity partidaStub = new PartidaEntity();
            partidaStub.setId(domain.getPartidaId());
            entity.setPartida(partidaStub);
        }
        
        // Map Date (Mapping Start Date to Single Date)
        // TODO: Domain uses date range (start/end) but Entity uses single date. 
        // Logic needs review for correct semantic mapping.
        entity.setFecha(domain.getFechaInicio());
        
        // Map Hours (Default as Domain doesn't have this field yet)
        entity.setHorasAsignadas(BigDecimal.ZERO);
        
        // Observations
        entity.setObservaciones(null);

        return entity;
    }

    public AsignacionActividad toDomain(AsignacionActividadEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("AsignacionActividadEntity cannot be null for mapping to domain object.");
        }

        return AsignacionActividad.crear(
            CuadrillaId.of(entity.getCuadrilla().getId()),
            ProyectoId.from(entity.getCuadrilla().getProyecto().getId()), // Assuming traverse to project
            entity.getPartida().getId(),
            entity.getFecha(), // Start Date
            entity.getFecha()  // End Date (No range in entity)
        );
    }
}
