package com.budgetpro.infrastructure.persistence.mapper.compra;

import com.budgetpro.domain.logistica.compra.model.Proveedor;
import com.budgetpro.domain.logistica.compra.model.ProveedorId;
import com.budgetpro.infrastructure.persistence.entity.compra.ProveedorEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Proveedor (dominio) y ProveedorEntity (persistencia).
 */
@Component
public class ProveedorMapper {

    /**
     * Convierte un Proveedor (dominio) a ProveedorEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public ProveedorEntity toEntity(Proveedor proveedor) {
        if (proveedor == null) {
            throw new IllegalArgumentException("Proveedor no puede ser null");
        }

        ProveedorEntity entity = new ProveedorEntity(
            proveedor.getId().getValue(),
            proveedor.getRazonSocial(),
            proveedor.getRuc(),
            proveedor.getEstado(),
            proveedor.getContacto(),
            proveedor.getDireccion(),
            null, // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
            proveedor.getCreatedBy(),
            proveedor.getUpdatedBy()
        );

        return entity;
    }

    /**
     * Convierte un ProveedorEntity (persistencia) a Proveedor (dominio).
     */
    public Proveedor toDomain(ProveedorEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("ProveedorEntity no puede ser null");
        }

        return Proveedor.reconstruir(
            ProveedorId.from(entity.getId()),
            entity.getRazonSocial(),
            entity.getRuc(),
            entity.getEstado(),
            entity.getContacto(),
            entity.getDireccion(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L,
            entity.getCreatedBy(),
            entity.getCreatedAt(),
            entity.getUpdatedBy(),
            entity.getUpdatedAt()
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(ProveedorEntity existingEntity, Proveedor proveedor) {
        existingEntity.setRazonSocial(proveedor.getRazonSocial());
        existingEntity.setRuc(proveedor.getRuc());
        existingEntity.setEstado(proveedor.getEstado());
        existingEntity.setContacto(proveedor.getContacto());
        existingEntity.setDireccion(proveedor.getDireccion());
        existingEntity.setUpdatedBy(proveedor.getUpdatedBy());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca id, createdBy, createdAt (son inmutables)
    }
}
