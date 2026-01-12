package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.finanzas.partida.Partida;
import com.budgetpro.domain.finanzas.partida.PartidaId;
import com.budgetpro.domain.finanzas.presupuesto.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.PresupuestoId;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para convertir entre PresupuestoEntity (capa de infraestructura) y Presupuesto (capa de dominio).
 * 
 * Realiza mapeo manual y explícito entre las dos representaciones.
 * Incluye mapeo de las Partida internas del agregado.
 * 
 * NOTA: Las Partida son entidades internas del agregado Presupuesto y se mapean como parte del agregado raíz.
 */
@Component
public class PresupuestoMapper {

    private final PartidaMapper partidaMapper;

    public PresupuestoMapper(PartidaMapper partidaMapper) {
        this.partidaMapper = partidaMapper;
    }

    /**
     * Convierte una PresupuestoEntity a un Presupuesto del dominio.
     * 
     * Usa el factory method `Presupuesto.reconstruir()` para reconstruir el agregado
     * completo incluyendo todas sus partidas internas.
     * 
     * @param entity La entidad JPA (con partidas cargadas)
     * @return El agregado del dominio con todas sus partidas
     * @throws IllegalArgumentException si la entidad es nula
     */
    public Presupuesto toDomain(PresupuestoEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        // Convertir Value Object desde primitivo
        PresupuestoId id = PresupuestoId.of(entity.getId());
        
        // Convertir Integer (JPA/ERD) a Long (dominio) para el factory method
        Long versionDomain = entity.getVersion() != null ? entity.getVersion().longValue() : 0L;

        // Mapear partidas internas del agregado
        List<Partida> partidasDomain = new ArrayList<>();
        if (entity.getPartidas() != null) {
            for (PartidaEntity partidaEntity : entity.getPartidas()) {
                // Pasar PresupuestoEntity para obtener proyectoId (requerido por PartidaMapper)
                Partida partidaDomain = partidaMapper.toDomain(partidaEntity, entity);
                partidasDomain.add(partidaDomain);
            }
        }

        // Reconstruir agregado usando factory method
        return Presupuesto.reconstruir(id, entity.getProyectoId(), entity.getEsContractual(), versionDomain, partidasDomain);
    }

    /**
     * Convierte un Presupuesto del dominio a una PresupuestoEntity.
     * 
     * Si se proporciona una entidad existente, la actualiza; de lo contrario, crea una nueva.
     * Las partidas se mapean y sincronizan con la colección de la entidad.
     * 
     * @param presupuesto El agregado del dominio (con sus partidas)
     * @param existingEntity La entidad existente (puede ser null para creación nueva)
     * @return La entidad JPA (nueva o actualizada) con sus partidas
     * @throws IllegalArgumentException si el parámetro requerido es nulo
     */
    public PresupuestoEntity toEntity(Presupuesto presupuesto, PresupuestoEntity existingEntity) {
        if (presupuesto == null) {
            throw new IllegalArgumentException("El presupuesto no puede ser nulo");
        }

        PresupuestoEntity entity;
        if (existingEntity != null) {
            // Actualizar entidad existente
            entity = existingEntity;
            entity.setProyectoId(presupuesto.getProyectoId());
            entity.setEsContractual(presupuesto.isEsContractual());
            // NO actualizamos: id (es inmutable)
            // NO actualizamos: version (Hibernate lo maneja automáticamente con @Version)

            // Sincronizar partidas: reemplazar completamente la colección
            sincronizarPartidas(entity, presupuesto);
        } else {
            // Crear nueva entidad
            // Convertir Long (dominio) a Integer (JPA/ERD) para la entidad
            // Para nuevas entidades, pasamos null para que Hibernate inicialice la versión
            Integer versionEntity = presupuesto.getVersion() != null ? presupuesto.getVersion().intValue() : null;
            entity = new PresupuestoEntity(
                presupuesto.getId().getValue(),
                presupuesto.getProyectoId(),
                presupuesto.isEsContractual(),
                versionEntity
            );

            // PASO 1: Mapear todas las partidas (sin establecer relaciones parent aún)
            for (Partida partida : presupuesto.getPartidas()) {
                PartidaEntity partidaEntity = partidaMapper.toEntity(partida, null, entity);
                entity.getPartidas().add(partidaEntity);
            }
            
            // PASO 2: Establecer relaciones parent ahora que todas las partidas están en la colección
            establecerRelacionesParent(entity, presupuesto);
        }

        return entity;
    }

    /**
     * Sincroniza las partidas de la entidad con las del dominio.
     * 
     * Estrategia: Reemplazar completamente la colección de partidas.
     * Esto funciona bien porque el cascade ALL + orphanRemoval maneja la persistencia correctamente.
     * 
     * NOTA: Por simplicidad y seguridad, usamos una estrategia de "replace all":
     * eliminamos todas las partidas existentes y recreamos desde el dominio.
     * Esto garantiza que la entidad JPA refleje exactamente el estado del agregado del dominio.
     * 
     * IMPORTANTE: Para establecer correctamente las relaciones parent en WBS, primero se mapean
     * todas las partidas, y luego se establecen las relaciones parent en un segundo paso.
     */
    private void sincronizarPartidas(PresupuestoEntity entity, Presupuesto presupuesto) {
        // Limpiar partidas existentes (orphanRemoval las eliminará de BD)
        entity.getPartidas().clear();

        // PASO 1: Mapear todas las partidas (sin establecer relaciones parent aún)
        for (Partida partida : presupuesto.getPartidas()) {
            PartidaEntity partidaEntity = partidaMapper.toEntity(partida, null, entity);
            entity.getPartidas().add(partidaEntity);
        }
        
        // PASO 2: Establecer relaciones parent ahora que todas las partidas están en la colección
        establecerRelacionesParent(entity, presupuesto);
    }
    
    /**
     * Establece las relaciones parent entre partidas después de mapearlas todas.
     * 
     * Esto es necesario porque las partidas hijas necesitan referenciar a sus padres,
     * pero los padres deben estar en la colección antes de establecer las relaciones.
     * 
     * @param entity La entidad PresupuestoEntity con todas las partidas mapeadas
     * @param presupuesto El agregado Presupuesto del dominio
     */
    private void establecerRelacionesParent(PresupuestoEntity entity, Presupuesto presupuesto) {
        for (Partida partida : presupuesto.getPartidas()) {
            if (partida.getParentId() != null) {
                // Buscar la entidad correspondiente en la colección
                PartidaEntity partidaEntity = entity.getPartidas().stream()
                        .filter(p -> p.getId().equals(partida.getId().getValue()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                            String.format("No se encontró PartidaEntity para partida %s", partida.getId())
                        ));
                
                // Buscar la entidad padre en la colección
                PartidaEntity parentEntity = entity.getPartidas().stream()
                        .filter(p -> p.getId().equals(partida.getParentId().getValue()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                            String.format("No se encontró PartidaEntity padre %s para partida %s", 
                                partida.getParentId(), partida.getId())
                        ));
                
                // Establecer la relación parent
                partidaEntity.setParent(parentEntity);
            }
        }
    }
}
