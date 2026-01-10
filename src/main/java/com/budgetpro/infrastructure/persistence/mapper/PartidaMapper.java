package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.finanzas.model.Monto;
import com.budgetpro.domain.finanzas.partida.CodigoPartida;
import com.budgetpro.domain.finanzas.partida.EstadoPartida;
import com.budgetpro.domain.finanzas.partida.Partida;
import com.budgetpro.domain.finanzas.partida.PartidaId;
import com.budgetpro.domain.recurso.model.TipoRecurso;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Mapper para convertir entre PartidaEntity (capa de infraestructura)
 * y Partida (capa de dominio).
 * 
 * Realiza mapeo manual y explícito entre las dos representaciones.
 * Convierte correctamente Monto (VO) ↔ BigDecimal (JPA) con escala 4.
 */
@Component
public class PartidaMapper {

    private static final int ESCALA = 4;
    private static final RoundingMode MODO_REDONDEO = RoundingMode.HALF_EVEN;

    /**
     * Convierte una PartidaEntity a un Partida del dominio.
     * 
     * Usa el factory method `reconstruir()` del dominio para cargar el estado desde BD.
     * 
     * @param entity La entidad JPA
     * @return El agregado del dominio
     * @throws IllegalArgumentException si la entidad es nula
     */
    public Partida toDomain(PartidaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        PartidaId id = PartidaId.of(entity.getId());
        CodigoPartida codigo = CodigoPartida.of(entity.getCodigo());
        Monto montoPresupuestado = toMonto(entity.getMontoPresupuestado());
        Monto montoReservado = toMonto(entity.getMontoReservado());
        Monto montoEjecutado = toMonto(entity.getMontoEjecutado());

        // Usar factory method reconstruir() del dominio (NO valida invariantes de creación)
        return Partida.reconstruir(
            id,
            entity.getProyectoId(),
            entity.getPresupuestoId(),
            codigo,
            entity.getNombre(),
            entity.getTipo(),
            montoPresupuestado,
            montoReservado,
            montoEjecutado,
            entity.getEstado(),
            entity.getVersion()
        );
    }

    /**
     * Convierte un Partida del dominio a una PartidaEntity.
     * 
     * Si la entidad ya existe (UPDATE), actualiza sus campos.
     * Si no existe (CREATE), crea una nueva.
     * 
     * @param partida El agregado del dominio
     * @param existingEntity La entidad existente (si existe) o null si es nueva
     * @param presupuestoEntity La entidad Presupuesto asociada (requerida para FK)
     * @return La entidad JPA
     * @throws IllegalArgumentException si el agregado es nulo
     */
    public PartidaEntity toEntity(Partida partida, PartidaEntity existingEntity, 
                                  com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity presupuestoEntity) {
        if (partida == null) {
            throw new IllegalArgumentException("La partida no puede ser nula");
        }
        if (presupuestoEntity == null) {
            throw new IllegalArgumentException("La entidad Presupuesto no puede ser nula");
        }

        PartidaEntity entity;
        if (existingEntity != null) {
            // UPDATE: Actualizar entidad existente
            entity = existingEntity;
        } else {
            // CREATE: Crear nueva entidad
            entity = new PartidaEntity();
            entity.setId(partida.getId().getValue());
        }

        // Actualizar todos los campos (excepto ID y version que se manejan automáticamente)
        entity.setPresupuesto(presupuestoEntity);
        entity.setProyectoId(partida.getProyectoId());
        entity.setCodigo(partida.getCodigo().getValue());
        entity.setNombre(partida.getNombre());
        entity.setTipo(partida.getTipo());
        entity.setMontoPresupuestado(toBigDecimal(partida.getMontoPresupuestado()));
        entity.setMontoReservado(toBigDecimal(partida.getMontoReservado()));
        entity.setMontoEjecutado(toBigDecimal(partida.getMontoEjecutado()));
        entity.setEstado(partida.getEstado());

        // NOTA: version se gestiona automáticamente por Hibernate con @Version

        return entity;
    }

    /**
     * Convierte un BigDecimal a un Monto (Value Object del dominio).
     * Aplica normalización a escala 4 decimales con redondeo HALF_EVEN.
     * 
     * @param value El BigDecimal (puede ser null, retorna Monto.cero())
     * @return Un Monto con escala 4 decimales
     */
    private Monto toMonto(BigDecimal value) {
        if (value == null) {
            return Monto.cero();
        }
        // Normalizar a escala 4 con HALF_EVEN
        BigDecimal normalized = value.setScale(ESCALA, MODO_REDONDEO);
        return Monto.of(normalized);
    }

    /**
     * Convierte un Monto (Value Object del dominio) a un BigDecimal.
     * Mantiene la escala 4 decimales (alineado con NUMERIC(19,4)).
     * 
     * @param monto El Monto (no puede ser null)
     * @return Un BigDecimal con escala 4 decimales
     */
    private BigDecimal toBigDecimal(Monto monto) {
        if (monto == null) {
            return BigDecimal.ZERO.setScale(ESCALA, MODO_REDONDEO);
        }
        // El Monto ya tiene escala 4, solo necesitamos el BigDecimal
        return monto.toBigDecimal();
    }
}
