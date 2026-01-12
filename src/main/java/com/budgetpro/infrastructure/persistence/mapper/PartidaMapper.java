package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.finanzas.model.Monto;
import com.budgetpro.domain.finanzas.partida.CodigoPartida;
import com.budgetpro.domain.finanzas.partida.EstadoPartida;
import com.budgetpro.domain.finanzas.partida.Partida;
import com.budgetpro.domain.finanzas.partida.PartidaId;
import com.budgetpro.domain.recurso.model.TipoRecurso;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper para convertir entre PartidaEntity (capa de infraestructura)
 * y Partida (capa de dominio).
 * 
 * Mapea solo los campos definidos en el ERD f?sico definitivo:
 * - id UUID
 * - presupuesto_id UUID
 * - codigo VARCHAR(50)
 * - descripcion TEXT
 * - created_at TIMESTAMP
 * - updated_at TIMESTAMP
 * 
 * NOTA: El dominio Partida tiene campos adicionales (tipo, montos, estado, version) que no est?n
 * en el ERD definitivo. Estos campos se manejan fuera del scope de persistencia del ERD.
 */
@Component
public class PartidaMapper {

    /**
     * Convierte una PartidaEntity a un Partida del dominio.
     * 
     * Mapea solo los campos definidos en el ERD definitivo:
     * - descripcion (ERD) -> nombre (dominio)
     * - codigo (ERD) -> codigo (dominio)
     * 
     * NOTA: Los campos adicionales del dominio (tipo, montos, estado, version) se manejan
     * con valores por defecto o se obtienen de otras fuentes fuera del scope del ERD.
     * 
     * @param entity La entidad JPA
     * @param presupuestoEntity La entidad Presupuesto (para obtener proyectoId)
     * @return El agregado del dominio
     * @throws IllegalArgumentException si la entidad es nula
     */
    public Partida toDomain(PartidaEntity entity, PresupuestoEntity presupuestoEntity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }
        if (presupuestoEntity == null) {
            throw new IllegalArgumentException("La entidad Presupuesto no puede ser nula (requerida para proyectoId)");
        }

        // Convertir Value Objects desde primitivos
        PartidaId id = PartidaId.of(entity.getId());
        CodigoPartida codigo = CodigoPartida.of(entity.getCodigo()); // Normaliza autom?ticamente (trim + uppercase)
        
        // Mapear descripcion (ERD) a nombre (dominio)
        String nombre = entity.getDescripcion();
        
        // Obtener proyectoId desde PresupuestoEntity
        UUID proyectoId = presupuestoEntity.getProyectoId();
        UUID presupuestoId = presupuestoEntity.getId();

        // NOTA: Los campos adicionales del dominio (tipo, montos, estado, version) no est?n en el ERD definitivo.
        // Estos campos se manejan con valores por defecto o se obtienen de otras fuentes.
        // Por ahora, usamos valores por defecto para permitir la reconstrucci?n del dominio.
        TipoRecurso tipo = TipoRecurso.MATERIAL; // Valor por defecto (no est? en ERD)
        Monto montoPresupuestado = Monto.cero(); // Valor por defecto (no est? en ERD)
        Monto montoReservado = Monto.cero(); // Valor por defecto (no est? en ERD)
        Monto montoEjecutado = Monto.cero(); // Valor por defecto (no est? en ERD)
        EstadoPartida estado = EstadoPartida.BORRADOR; // Valor por defecto (no est? en ERD)
        Long version = 0L; // Valor por defecto (no est? en ERD)

        // WBS: Obtener parent_id y nivel desde la entidad
        UUID parentIdValue = entity.getParentId();
        PartidaId parentId = parentIdValue != null ? PartidaId.of(parentIdValue) : null;
        int nivel = entity.getNivel() != null ? entity.getNivel() : 1; // Default 1 si no est? en BD
        
        // Reconstruir agregado usando factory method
        return Partida.reconstruir(
            id,
            proyectoId,
            presupuestoId,
            codigo,
            nombre,
            tipo,
            montoPresupuestado,
            montoReservado,
            montoEjecutado,
            estado,
            version,
            parentId,
            nivel
        );
    }

    /**
     * Convierte un Partida del dominio a una PartidaEntity.
     * 
     * Mapea solo los campos definidos en el ERD definitivo:
     * - nombre (dominio) -> descripcion (ERD)
     * - codigo (dominio) -> codigo (ERD)
     * 
     * NOTA: Los campos adicionales del dominio (tipo, montos, estado, version) no se persisten
     * porque no est?n en el ERD definitivo.
     * 
     * @param partida El agregado del dominio
     * @param existingEntity La entidad existente (puede ser null para creaci?n nueva)
     * @param presupuestoEntity La entidad Presupuesto asociada (requerida para FK)
     * @return La entidad JPA (nueva o actualizada)
     * @throws IllegalArgumentException si alguno de los par?metros requeridos es nulo
     */
    public PartidaEntity toEntity(Partida partida, PartidaEntity existingEntity, PresupuestoEntity presupuestoEntity) {
        if (partida == null) {
            throw new IllegalArgumentException("La partida no puede ser nula");
        }
        if (presupuestoEntity == null) {
            throw new IllegalArgumentException("La entidad Presupuesto no puede ser nula (requerida para FK)");
        }

        PartidaEntity entity;
        if (existingEntity != null) {
            // Actualizar entidad existente
            entity = existingEntity;
            
            // Actualizar solo campos definidos en el ERD definitivo
            entity.setPresupuesto(presupuestoEntity); // Asegurar relaci?n
            entity.setCodigo(partida.getCodigo().getValue()); // Ya est? normalizado
            // Mapear nombre (dominio) a descripcion (ERD)
            entity.setDescripcion(partida.getNombre());
            // WBS: Actualizar parent_id y nivel
            establecerParent(entity, partida, presupuestoEntity);
            entity.setNivel(partida.getNivel());
        } else {
            // Crear nueva entidad usando constructor p?blico con campos del ERD definitivo
            entity = new PartidaEntity(
                partida.getId().getValue(),
                presupuestoEntity,
                partida.getCodigo().getValue(), // Ya est? normalizado en CodigoPartida
                partida.getNombre(), // Mapear nombre (dominio) a descripcion (ERD)
                partida.getParentId() != null ? partida.getParentId().getValue() : null, // WBS: parent_id
                partida.getNivel() // WBS: nivel
            );
            
            // WBS: Establecer relaci?n @ManyToOne con el padre si existe
            establecerParent(entity, partida, presupuestoEntity);
        }

        return entity;
    }
    
    /**
     * Establece la relaci?n @ManyToOne con la partida padre si existe.
     * 
     * Busca la entidad padre en la colecci?n de partidas del presupuesto y establece la relaci?n.
     * Si no se encuentra en la colecci?n, busca en todas las partidas del presupuesto (incluyendo las ya mapeadas).
     * 
     * @param entity La entidad PartidaEntity a actualizar
     * @param partida La partida del dominio (para obtener parentId)
     * @param presupuestoEntity La entidad Presupuesto (para buscar el padre en su colecci?n)
     */
    private void establecerParent(PartidaEntity entity, Partida partida, PresupuestoEntity presupuestoEntity) {
        if (partida.getParentId() != null) {
            // Buscar la entidad padre en la colecci?n de partidas del presupuesto
            // Primero buscar en las partidas ya mapeadas
            PartidaEntity parentEntity = presupuestoEntity.getPartidas().stream()
                    .filter(p -> p.getId().equals(partida.getParentId().getValue()))
                    .findFirst()
                    .orElse(null);
            
            if (parentEntity != null) {
                entity.setParent(parentEntity);
            } else {
                // Si no se encuentra en la colecci?n, puede ser que a?n no se haya mapeado
                // En este caso, JPA establecer? el parent_id correctamente desde el UUID
                // cuando se persista, pero necesitamos establecer la relaci?n para que JPA la maneje
                // Por ahora, dejamos parent como null y JPA manejar? el parent_id desde el constructor
                // El parent_id ya se estableci? en el constructor de PartidaEntity
                entity.setParent(null);
                // NOTA: JPA establecer? correctamente el parent_id desde la columna parent_id
                // cuando se persista, incluso si parent es null
            }
        } else {
            // Partida ra?z: sin padre
            entity.setParent(null);
        }
    }
}
