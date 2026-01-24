package com.budgetpro.infrastructure.persistence.mapper.requisicion;

import com.budgetpro.domain.logistica.requisicion.model.Requisicion;
import com.budgetpro.domain.logistica.requisicion.model.RequisicionId;
import com.budgetpro.domain.logistica.requisicion.model.RequisicionItem;
import com.budgetpro.infrastructure.persistence.entity.requisicion.RequisicionEntity;
import com.budgetpro.infrastructure.persistence.entity.requisicion.RequisicionItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Requisicion (dominio) y RequisicionEntity (persistencia).
 */
@Component
public class RequisicionMapper {

    /**
     * Convierte un Requisicion (dominio) a RequisicionEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public RequisicionEntity toEntity(Requisicion requisicion) {
        if (requisicion == null) {
            return null;
        }

        RequisicionEntity entity = new RequisicionEntity(
            requisicion.getId().getValue(),
            requisicion.getProyectoId(),
            requisicion.getSolicitante(),
            requisicion.getFrenteTrabajo(),
            requisicion.getFechaSolicitud(),
            requisicion.getAprobadoPor(),
            requisicion.getEstado(),
            requisicion.getObservaciones(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );

        // Mapear ítems
        List<RequisicionItemEntity> itemsEntities = requisicion.getItems().stream()
                .map(item -> toItemEntity(item, entity))
                .collect(Collectors.toList());
        entity.setItems(itemsEntities);

        return entity;
    }

    /**
     * Convierte un RequisicionItem (dominio) a RequisicionItemEntity (persistencia).
     */
    public RequisicionItemEntity toItemEntity(RequisicionItem item, RequisicionEntity requisicionEntity) {
        if (item == null) {
            return null;
        }

        return new RequisicionItemEntity(
            item.getId().getValue(),
            requisicionEntity,
            item.getRecursoExternalId(),
            item.getPartidaId(),
            item.getCantidadSolicitada(),
            item.getCantidadDespachada(),
            item.getUnidadMedida(),
            item.getJustificacion(),
            null // CRÍTICO: null para nuevas entidades, Hibernate lo manejará
        );
    }

    /**
     * Convierte un RequisicionEntity (persistencia) a Requisicion (dominio).
     */
    public Requisicion toDomain(RequisicionEntity entity) {
        if (entity == null) {
            return null;
        }

        // Mapear ítems
        List<RequisicionItem> items = entity.getItems().stream()
                .map(this::toItemDomain)
                .collect(Collectors.toList());

        return Requisicion.reconstruir(
            RequisicionId.from(entity.getId()),
            entity.getProyectoId(),
            entity.getSolicitante(),
            entity.getFrenteTrabajo(),
            entity.getFechaSolicitud(),
            entity.getAprobadoPor(),
            entity.getEstado(),
            entity.getObservaciones(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L,
            items
        );
    }

    /**
     * Convierte un RequisicionItemEntity (persistencia) a RequisicionItem (dominio).
     */
    public RequisicionItem toItemDomain(RequisicionItemEntity entity) {
        if (entity == null) {
            return null;
        }

        return RequisicionItem.reconstruir(
            com.budgetpro.domain.logistica.requisicion.model.RequisicionItemId.from(entity.getId()),
            entity.getRecursoExternalId(),
            entity.getPartidaId(),
            entity.getCantidadSolicitada(),
            entity.getCantidadDespachada(),
            entity.getUnidadMedida(),
            entity.getJustificacion()
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(RequisicionEntity existingEntity, Requisicion requisicion) {
        existingEntity.setSolicitante(requisicion.getSolicitante());
        existingEntity.setFrenteTrabajo(requisicion.getFrenteTrabajo());
        existingEntity.setFechaSolicitud(requisicion.getFechaSolicitud());
        existingEntity.setAprobadoPor(requisicion.getAprobadoPor());
        existingEntity.setEstado(requisicion.getEstado());
        existingEntity.setObservaciones(requisicion.getObservaciones());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca proyectoId (es inmutable después de crear)
        // Los ítems se sincronizan en el adapter
    }

    /**
     * Actualiza un RequisicionItemEntity existente con los datos del dominio.
     * Usado para actualizar cantidadDespachada cuando se registra un despacho.
     */
    public void updateItemEntity(RequisicionItemEntity existingItemEntity, RequisicionItem item) {
        existingItemEntity.setCantidadDespachada(item.getCantidadDespachada());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se tocan campos inmutables: recursoExternalId, partidaId, cantidadSolicitada, unidadMedida
    }
}
