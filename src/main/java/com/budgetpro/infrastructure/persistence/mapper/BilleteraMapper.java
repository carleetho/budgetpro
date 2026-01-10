package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.finanzas.billetera.Billetera;
import com.budgetpro.domain.finanzas.billetera.BilleteraId;
import com.budgetpro.domain.finanzas.billetera.Monto;
import com.budgetpro.domain.finanzas.billetera.Movimiento;
import com.budgetpro.domain.finanzas.billetera.TipoMovimiento;
import com.budgetpro.infrastructure.persistence.entity.BilleteraEntity;
import com.budgetpro.infrastructure.persistence.entity.MovimientoEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre BilleteraEntity y MovimientoEntity (capa de infraestructura)
 * y Billetera y Movimiento (capa de dominio).
 * 
 * Realiza mapeo manual y explícito entre las dos representaciones.
 * 
 * IMPORTANTE: Convierte Monto (Value Object) ↔ BigDecimal manteniendo escala 4.
 */
@Component
public class BilleteraMapper {

    /**
     * Convierte una BilleteraEntity a un Billetera del dominio.
     * 
     * NOTA: No incluye los movimientos en esta conversión.
     * Los movimientos deben ser cargados por separado y agregados al agregado.
     * 
     * @param entity La entidad JPA de la billetera
     * @return El agregado del dominio reconstruido
     * @throws IllegalArgumentException si la entidad es nula
     */
    public Billetera toDomain(BilleteraEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad BilleteraEntity no puede ser nula");
        }

        BilleteraId id = BilleteraId.of(entity.getId());
        Monto saldoActual = Monto.of(entity.getSaldoActual());

        // Reconstruir el agregado usando el factory method de reconstrucción
        // Esto NO dispara eventos de dominio
        return Billetera.reconstruir(
            id,
            entity.getProyectoId(),
            saldoActual,
            entity.getVersion()
        );
    }

    /**
     * Convierte una MovimientoEntity a un Movimiento del dominio.
     * 
     * @param entity La entidad JPA del movimiento
     * @return El Movimiento del dominio reconstruido
     * @throws IllegalArgumentException si la entidad es nula
     */
    public Movimiento toDomain(MovimientoEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad MovimientoEntity no puede ser nula");
        }

        UUID billeteraIdValue = entity.getBilleteraId();
        if (billeteraIdValue == null) {
            throw new IllegalArgumentException("La entidad MovimientoEntity debe tener un billeteraId válido");
        }

        BilleteraId billeteraId = BilleteraId.of(billeteraIdValue);
        Monto monto = Monto.of(entity.getMonto());

        // Reconstruir el movimiento usando el factory method de reconstrucción
        // NOTA: El campo estado de la entidad no se mapea al dominio porque Movimiento no tiene estado
        return Movimiento.reconstruir(
            entity.getId(),
            billeteraId,
            monto,
            entity.getTipo(),
            entity.getFecha(),
            entity.getReferencia(),
            entity.getEvidenciaUrl()
        );
    }

    /**
     * Convierte una lista de MovimientoEntity a una lista de Movimiento del dominio.
     * 
     * @param entities Lista de entidades JPA
     * @return Lista de Movimientos del dominio
     */
    public List<Movimiento> toDomainMovimientos(List<MovimientoEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un Billetera del dominio a una BilleteraEntity.
     * 
     * @param billetera El agregado del dominio
     * @return La entidad JPA
     * @throws IllegalArgumentException si el agregado es nulo
     */
    public BilleteraEntity toEntity(Billetera billetera) {
        if (billetera == null) {
            throw new IllegalArgumentException("El agregado Billetera no puede ser nulo");
        }

        BilleteraEntity entity = new BilleteraEntity();
        entity.setId(billetera.getId().getValue());
        entity.setProyectoId(billetera.getProyectoId());
        
        // Convertir Monto (Value Object) a BigDecimal (escala 4 ya está garantizada por Monto)
        entity.setSaldoActual(billetera.getSaldoActual().toBigDecimal());
        
        // Version: si es null (creación nueva), se establece en 0 para insert
        // Si no es null (reconstrucción), se mantiene para UPDATE con optimistic locking
        entity.setVersion(billetera.getVersion() != null ? billetera.getVersion() : 0L);

        return entity;
    }

    /**
     * Convierte un Movimiento del dominio a una MovimientoEntity.
     * 
     * @param movimiento El Movimiento del dominio
     * @return La entidad JPA
     * @throws IllegalArgumentException si el movimiento es nulo
     */
    /**
     * Convierte un Movimiento del dominio a una MovimientoEntity.
     * 
     * IMPORTANTE: Este método NO establece la relación bidireccional con BilleteraEntity.
     * La relación debe establecerse mediante el método helper agregarMovimiento() de BilleteraEntity
     * o estableciendo directamente la referencia billetera en la entidad.
     * 
     * @param movimiento El Movimiento del dominio
     * @return La entidad JPA (sin relación bidireccional establecida)
     * @throws IllegalArgumentException si el movimiento es nulo
     */
    public MovimientoEntity toEntity(Movimiento movimiento) {
        if (movimiento == null) {
            throw new IllegalArgumentException("El Movimiento no puede ser nulo");
        }

        MovimientoEntity entity = new MovimientoEntity();
        entity.setId(movimiento.getId());
        // NO establecer billetera aquí - debe hacerse desde BilleteraEntity.agregarMovimiento()
        
        // Convertir Monto (Value Object) a BigDecimal (escala 4 ya está garantizada por Monto)
        entity.setMonto(movimiento.getMonto().toBigDecimal());
        
        entity.setTipo(movimiento.getTipo());
        entity.setFecha(movimiento.getFecha());
        entity.setReferencia(movimiento.getReferencia());
        entity.setEvidenciaUrl(movimiento.getEvidenciaUrl());
        // Estado por defecto: ACTIVO (el dominio no maneja estado, pero la BD sí)
        entity.setEstado("ACTIVO");

        return entity;
    }

    /**
     * Convierte un Movimiento del dominio a una MovimientoEntity estableciendo la relación bidireccional.
     * 
     * @param movimiento El Movimiento del dominio
     * @param billeteraEntity La entidad BilleteraEntity padre
     * @return La entidad JPA con relación bidireccional establecida
     * @throws IllegalArgumentException si alguno de los parámetros es nulo
     */
    public MovimientoEntity toEntity(Movimiento movimiento, BilleteraEntity billeteraEntity) {
        if (movimiento == null) {
            throw new IllegalArgumentException("El Movimiento no puede ser nulo");
        }
        if (billeteraEntity == null) {
            throw new IllegalArgumentException("La BilleteraEntity no puede ser nula");
        }

        MovimientoEntity entity = toEntity(movimiento);
        // Establecer relación bidireccional
        entity.setBilletera(billeteraEntity);
        
        return entity;
    }

    /**
     * Convierte una lista de Movimiento del dominio a una lista de MovimientoEntity.
     * 
     * @param movimientos Lista de Movimientos del dominio
     * @return Lista de entidades JPA (sin relación bidireccional establecida)
     */
    public List<MovimientoEntity> toEntityMovimientos(List<Movimiento> movimientos) {
        if (movimientos == null) {
            return List.of();
        }
        return movimientos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de Movimiento del dominio a una lista de MovimientoEntity estableciendo la relación bidireccional.
     * 
     * @param movimientos Lista de Movimientos del dominio
     * @param billeteraEntity La entidad BilleteraEntity padre
     * @return Lista de entidades JPA con relación bidireccional establecida
     */
    public List<MovimientoEntity> toEntityMovimientos(List<Movimiento> movimientos, BilleteraEntity billeteraEntity) {
        if (movimientos == null) {
            return List.of();
        }
        return movimientos.stream()
                .map(mov -> toEntity(mov, billeteraEntity))
                .collect(Collectors.toList());
    }

    /**
     * Actualiza una entidad existente con los datos del agregado del dominio.
     * Útil para operaciones de actualización.
     * 
     * IMPORTANTE: No actualiza el campo `version` (lo maneja Hibernate automáticamente con @Version).
     * 
     * @param entity La entidad existente a actualizar
     * @param billetera El agregado del dominio con los nuevos datos
     * @throws IllegalArgumentException si alguno de los parámetros es nulo
     */
    public void updateEntity(BilleteraEntity entity, Billetera billetera) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad BilleteraEntity no puede ser nula");
        }
        if (billetera == null) {
            throw new IllegalArgumentException("El agregado Billetera no puede ser nulo");
        }

        // Actualizamos solo los campos mutables
        entity.setSaldoActual(billetera.getSaldoActual().toBigDecimal());
        
        // No actualizamos: id, proyectoId, version (version lo maneja Hibernate automáticamente)
        // updatedAt se actualiza automáticamente por @UpdateTimestamp
    }
}
