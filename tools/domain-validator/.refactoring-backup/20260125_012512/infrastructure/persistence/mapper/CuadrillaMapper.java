package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.logistica.organizacion.model.Cuadrilla;
import com.budgetpro.domain.logistica.organizacion.model.CuadrillaId;
import com.budgetpro.domain.logistica.organizacion.model.FrenteTrabajoId;
import com.budgetpro.infrastructure.persistence.entity.rrhh.CuadrillaEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.EmpleadoEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CuadrillaMapper {

    // Placeholder for missing FrenteTrabajoId in DB schema
    private static final UUID DUMMY_FRENTE_TRABAJO_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public Cuadrilla toDomain(CuadrillaEntity entity) {
        if (entity == null) {
            return null;
        }

        String capatazName = "Unknown";
        if (entity.getCapataz() != null) {
            capatazName = entity.getCapataz().getNombre() + " " + entity.getCapataz().getApellido();
        }

        return Cuadrilla.reconstruir(CuadrillaId.of(entity.getId()), entity.getProyecto().getId(), entity.getCodigo(),
                entity.getNombre(), capatazName, FrenteTrabajoId.of(DUMMY_FRENTE_TRABAJO_ID), // FIXME: Schema mismatch
                "ACTIVA".equalsIgnoreCase(entity.getEstado()));
    }

    public CuadrillaEntity toEntity(Cuadrilla domain) {
        if (domain == null) {
            return null;
        }

        CuadrillaEntity entity = new CuadrillaEntity();
        entity.setId(domain.getId().getValue());

        // We cannot fully reconstruct Entity dependencies (Proyecto, Capataz) from
        // ID/String here
        // without Repository access. Mappers should usually be simple.
        // For 'toEntity', typically we set IDs if using reference mapping, or we need
        // to fetch entities.
        // Assuming this is used for saving, where we might merge.
        // But JPA Repositories usually save ENTITIES.
        // We set the IDs solely if we assume we have references?
        // CuadrillaEntity need ProyectoEntity object, not just ID.
        // Standard pattern: The user invoking the mapper (e.g. Use Case) should load
        // dependencies
        // OR we map what we can and leave relations null/proxy.
        // However, standard simplistic mapping:

        ProyectoEntity proyectoProxy = new ProyectoEntity();
        proyectoProxy.setId(domain.getProyectoId());
        entity.setProyecto(proyectoProxy);

        entity.setNombre(domain.getNombre());
        entity.setCodigo(domain.getCodigo());
        entity.setEstado(domain.isActiva() ? "ACTIVA" : "DISUELTA");

        // Capataz is a String in Domain. Entity needs EmpleadoEntity.
        // We CANNOT map String -> EmpleadoEntity here.
        // This direction is LOSSYS.
        // We'll leave capataz null or require explicit setting by caller.

        return entity;
    }
}
