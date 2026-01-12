package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.infrastructure.persistence.entity.BilleteraEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Mapper para convertir entre BilleteraEntity (capa de infraestructura) y Billetera (capa de dominio).
 * 
 * Realiza mapeo manual y explícito entre las dos representaciones.
 * 
 * NOTA: Los movimientos de caja se mapean por separado usando MovimientoCajaMapper.
 * Este mapper solo maneja la entidad Billetera (saldo, versión, etc.).
 */
@Component
public class BilleteraMapper {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /**
     * Convierte una BilleteraEntity a un Billetera del dominio.
     * 
     * Usa el factory method `Billetera.reconstruir()` para reconstruir el agregado
     * desde persistencia (NO incluye movimientos; estos se cargan por separado si se necesitan).
     * 
     * @param entity La entidad JPA
     * @return El agregado del dominio
     * @throws IllegalArgumentException si la entidad es nula
     */
    public Billetera toDomain(BilleteraEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        // Convertir Value Object desde primitivo
        BilleteraId id = BilleteraId.of(entity.getId());
        
        // Normalizar saldo a escala 4
        BigDecimal saldoActual = normalizarBigDecimal(entity.getSaldoActual());

        // Reconstruir agregado usando factory method
        // Los movimientos se cargan por separado si se necesitan (no se incluyen en la reconstrucción básica)
        // Convertir Integer (JPA/ERD) a Long (dominio) para el factory method
        Long versionDomain = entity.getVersion() != null ? entity.getVersion().longValue() : 0L;
        return Billetera.reconstruir(id, entity.getProyectoId(), saldoActual, versionDomain);
    }


    /**
     * Convierte un Billetera del dominio a una BilleteraEntity.
     * 
     * Si se proporciona una entidad existente, la actualiza; de lo contrario, crea una nueva.
     * 
     * @param billetera El agregado del dominio
     * @param existingEntity La entidad existente (puede ser null para creación nueva)
     * @return La entidad JPA (nueva o actualizada)
     * @throws IllegalArgumentException si el parámetro requerido es nulo
     */
    public BilleteraEntity toEntity(Billetera billetera, BilleteraEntity existingEntity) {
        if (billetera == null) {
            throw new IllegalArgumentException("La billetera no puede ser nula");
        }

        BilleteraEntity entity;
        if (existingEntity != null) {
            // Actualizar entidad existente
            entity = existingEntity;
            entity.setSaldoActual(billetera.getSaldoActual());
            
            // NO actualizamos: id, proyectoId (son inmutables)
        } else {
            // Crear nueva entidad
            entity = new BilleteraEntity(
                billetera.getId().getValue(),
                billetera.getProyectoId(),
                billetera.getSaldoActual(),
                null // <--- CORRECCIÓN: Pasamos null para indicar "Nueva Entidad"
            );
        }

        return entity;
    }


    /**
     * Normaliza un BigDecimal a escala 4 con redondeo HALF_EVEN (Banker's Rounding).
     * Garantiza que siempre se use la escala correcta para NUMERIC(19,4).
     */
    private static BigDecimal normalizarBigDecimal(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        }
        return value.setScale(SCALE, ROUNDING_MODE);
    }
}
