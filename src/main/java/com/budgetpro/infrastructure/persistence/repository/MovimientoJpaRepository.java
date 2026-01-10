package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.BilleteraEntity;
import com.budgetpro.infrastructure.persistence.entity.MovimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad MovimientoEntity.
 * Proporciona operaciones de persistencia para movimientos de caja.
 */
@Repository
public interface MovimientoJpaRepository extends JpaRepository<MovimientoEntity, UUID> {

    /**
     * Busca todos los movimientos de una billetera ordenados por fecha descendente.
     * Usa la relación bidireccional mediante la propiedad anidada.
     * 
     * @param billeteraId El ID de la billetera
     * @return Lista de movimientos ordenados por fecha más reciente primero
     */
    @Query("SELECT m FROM MovimientoEntity m WHERE m.billetera.id = :billeteraId ORDER BY m.fecha DESC")
    List<MovimientoEntity> findByBilleteraIdOrderByFechaDesc(@Param("billeteraId") UUID billeteraId);

    /**
     * Busca todos los movimientos de una billetera mediante la relación bidireccional.
     * 
     * @param billetera La entidad BilleteraEntity
     * @return Lista de movimientos ordenados por fecha más reciente primero
     */
    List<MovimientoEntity> findByBilleteraOrderByFechaDesc(BilleteraEntity billetera);

    /**
     * Cuenta el número de movimientos de una billetera.
     * 
     * @param billeteraId El ID de la billetera
     * @return El número de movimientos
     */
    @Query("SELECT COUNT(m) FROM MovimientoEntity m WHERE m.billetera.id = :billeteraId")
    long countByBilleteraId(@Param("billeteraId") UUID billeteraId);
}
