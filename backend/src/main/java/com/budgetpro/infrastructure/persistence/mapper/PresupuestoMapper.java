package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Presupuesto (dominio) y PresupuestoEntity (persistencia).
 */
@Component
public class PresupuestoMapper {

    /**
     * Convierte un Presupuesto (dominio) a PresupuestoEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     * 
     * Incluye mapeo de campos de integrity hash si el presupuesto fue aprobado.
     */
    public PresupuestoEntity toEntity(Presupuesto presupuesto) {
        if (presupuesto == null) {
            return null;
        }

        PresupuestoEntity entity = new PresupuestoEntity(
            presupuesto.getId().getValue(),
            presupuesto.getProyectoId(),
            presupuesto.getNombre(),
            presupuesto.getEstado(),
            presupuesto.getEsContractual(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );

        // Mapear campos de integrity hash (pueden ser null para presupuestos no aprobados)
        entity.setIntegrityHashApproval(presupuesto.getIntegrityHashApproval());
        entity.setIntegrityHashExecution(presupuesto.getIntegrityHashExecution());
        entity.setIntegrityHashGeneratedAt(presupuesto.getIntegrityHashGeneratedAt());
        entity.setIntegrityHashGeneratedBy(presupuesto.getIntegrityHashGeneratedBy());
        entity.setIntegrityHashAlgorithm(presupuesto.getIntegrityHashAlgorithm());

        aplicarCabecera(entity, presupuesto);

        return entity;
    }

    /**
     * Convierte un PresupuestoEntity (persistencia) a Presupuesto (dominio).
     * 
     * Usa el método reconstruir() con campos de integrity hash si están presentes.
     * Maneja valores null para presupuestos que no han sido aprobados aún.
     */
    public Presupuesto toDomain(PresupuestoEntity entity) {
        if (entity == null) {
            return null;
        }

        // Usar el método reconstruir() que incluye campos de integrity hash
        // Si los campos son null (presupuesto no aprobado), se pasan como null
        return Presupuesto.reconstruir(
            PresupuestoId.from(entity.getId()),
            entity.getProyectoId(),
            entity.getNombre(),
            entity.getEstado(),
            entity.getEsContractual(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L,
            entity.getIntegrityHashApproval(),      // Puede ser null
            entity.getIntegrityHashExecution(),      // Puede ser null
            entity.getIntegrityHashGeneratedAt(),    // Puede ser null
            entity.getIntegrityHashGeneratedBy(),    // Puede ser null
            entity.getIntegrityHashAlgorithm(),       // Puede ser null
            cabeceraDesde(entity)
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     * 
     * Incluye actualización de campos de integrity hash si el presupuesto fue aprobado.
     */
    public void updateEntity(PresupuestoEntity existingEntity, Presupuesto presupuesto) {
        existingEntity.setNombre(presupuesto.getNombre());
        existingEntity.setEstado(presupuesto.getEstado());
        existingEntity.setEsContractual(presupuesto.getEsContractual());
        
        // Actualizar campos de integrity hash (pueden ser null para presupuestos no aprobados)
        existingEntity.setIntegrityHashApproval(presupuesto.getIntegrityHashApproval());
        existingEntity.setIntegrityHashExecution(presupuesto.getIntegrityHashExecution());
        existingEntity.setIntegrityHashGeneratedAt(presupuesto.getIntegrityHashGeneratedAt());
        existingEntity.setIntegrityHashGeneratedBy(presupuesto.getIntegrityHashGeneratedBy());
        existingEntity.setIntegrityHashAlgorithm(presupuesto.getIntegrityHashAlgorithm());

        aplicarCabecera(existingEntity, presupuesto);
        
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
    }

    private static Presupuesto.CabeceraOpcionB cabeceraDesde(PresupuestoEntity entity) {
        return new Presupuesto.CabeceraOpcionB(
                entity.getCodigo(),
                entity.getClienteId(),
                entity.getDistritoId(),
                entity.getFechaElaboracion(),
                entity.getPlazoDias(),
                entity.getJornadaDiaria(),
                entity.getMonedaBaseId(),
                entity.getMonedaAlternaId(),
                entity.getFactorCambio(),
                entity.getRequiereFormulaPolinomica(),
                entity.getTipoApu(),
                entity.getDecimalesPrecios(),
                entity.getDecimalesMetrados(),
                entity.getDecimalesIncidencias(),
                entity.getEsContractualVigente());
    }

    private static void aplicarCabecera(PresupuestoEntity entity, Presupuesto presupuesto) {
        Presupuesto.CabeceraOpcionB cab = presupuesto.getCabeceraOpcionB();
        if (cab == null) {
            return;
        }
        entity.setCodigo(cab.codigo());
        entity.setClienteId(cab.clienteId());
        entity.setDistritoId(cab.distritoId());
        entity.setFechaElaboracion(cab.fechaElaboracion());
        entity.setPlazoDias(cab.plazoDias());
        if (cab.jornadaDiaria() != null) {
            entity.setJornadaDiaria(cab.jornadaDiaria());
        }
        entity.setMonedaBaseId(cab.monedaBaseId());
        entity.setMonedaAlternaId(cab.monedaAlternaId());
        entity.setFactorCambio(cab.factorCambio());
        if (cab.requiereFormulaPolinomica() != null) {
            entity.setRequiereFormulaPolinomica(cab.requiereFormulaPolinomica());
        }
        if (cab.tipoApu() != null) {
            entity.setTipoApu(cab.tipoApu());
        }
        entity.setDecimalesPrecios(cab.decimalesPrecios());
        entity.setDecimalesMetrados(cab.decimalesMetrados());
        entity.setDecimalesIncidencias(cab.decimalesIncidencias());
        if (cab.esContractualVigente() != null) {
            entity.setEsContractualVigente(cab.esContractualVigente());
        }
    }
}
