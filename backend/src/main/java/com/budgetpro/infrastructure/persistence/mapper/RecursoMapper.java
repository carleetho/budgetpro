package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.recurso.model.EstadoRecurso;
import com.budgetpro.domain.recurso.model.Recurso;
import com.budgetpro.domain.recurso.model.RecursoId;
import com.budgetpro.domain.shared.model.TipoRecurso;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mapper para convertir entre RecursoEntity (capa de infraestructura) y Recurso
 * (capa de dominio).
 * 
 * Realiza mapeo manual y explícito entre las dos representaciones.
 */
@Component
public class RecursoMapper {

    /**
     * Convierte una RecursoEntity a un Recurso del dominio.
     * 
     * @param entity La entidad JPA
     * @return El agregado del dominio
     * @throws IllegalArgumentException si la entidad es nula
     */
    public Recurso toDomain(RecursoEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        RecursoId id = RecursoId.of(entity.getId());
        Map<String, Object> atributos = entity.getAtributos() != null ? new HashMap<>(entity.getAtributos())
                : new HashMap<>();

        // Construimos el Recurso usando el factory method del dominio
        // Usamos nombreNormalizado ya que es el campo canónico con constraint UNIQUE
        // El dominio normalizará el nombre nuevamente (idempotente)
        Recurso recurso = Recurso.crear(id, entity.getNombreNormalizado(), entity.getTipo(), entity.getUnidadBase());

        // Actualizamos los atributos si existen
        if (!atributos.isEmpty()) {
            recurso.actualizarAtributos(atributos);
        }

        // Establecemos el estado manualmente (ya que el factory method establece ACTIVO
        // por defecto)
        if (entity.getEstado() != EstadoRecurso.ACTIVO) {
            switch (entity.getEstado()) {
            case EN_REVISION -> recurso.marcarEnRevision();
            case DEPRECADO -> recurso.desactivar();
            default -> recurso.activar();
            }
        }

        return recurso;
    }

    /**
     * Convierte un Recurso del dominio a una RecursoEntity.
     * 
     * @param recurso   El agregado del dominio
     * @param createdBy El UUID del usuario que crea el recurso (requerido por el
     *                  ERD)
     * @return La entidad JPA
     * @throws IllegalArgumentException si el recurso es nulo o createdBy es nulo
     */
    public RecursoEntity toEntity(Recurso recurso, UUID createdBy) {
        if (recurso == null) {
            throw new IllegalArgumentException("El recurso no puede ser nulo");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("El createdBy no puede ser nulo");
        }

        RecursoEntity entity = new RecursoEntity();
        entity.setId(recurso.getId().getValue());
        entity.setNombre(recurso.getNombre());
        // El nombre normalizado es igual al nombre ya que el dominio normaliza
        // automáticamente
        entity.setNombreNormalizado(recurso.getNombre());
        entity.setTipo(recurso.getTipo());
        entity.setUnidadBase(recurso.getUnidadBase());
        entity.setAtributos(recurso.getAtributos());
        entity.setEstado(recurso.getEstado());
        entity.setCreatedBy(createdBy);

        return entity;
    }

    /**
     * Actualiza una entidad existente con los datos del recurso del dominio. Útil
     * para operaciones de actualización.
     * 
     * @param entity  La entidad existente a actualizar
     * @param recurso El agregado del dominio con los nuevos datos
     * @throws IllegalArgumentException si alguno de los parámetros es nulo
     */
    public void updateEntity(RecursoEntity entity, Recurso recurso) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }
        if (recurso == null) {
            throw new IllegalArgumentException("El recurso no puede ser nulo");
        }

        // Actualizamos solo los campos mutables del dominio
        entity.setNombre(recurso.getNombre());
        entity.setNombreNormalizado(recurso.getNombre()); // El nombre normalizado es igual al nombre normalizado
        entity.setUnidadBase(recurso.getUnidadBase());
        entity.setAtributos(recurso.getAtributos());
        entity.setEstado(recurso.getEstado());
        // No actualizamos: id, createdAt, createdBy (son inmutables)
        // updatedAt se actualiza automáticamente por @UpdateTimestamp
    }
}
