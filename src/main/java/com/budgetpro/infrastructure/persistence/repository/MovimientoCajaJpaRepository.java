package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.MovimientoCajaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository para MovimientoCajaEntity.
 * 
 * Proporciona métodos de acceso a datos para la entidad MovimientoCaja.
 */
@Repository
public interface MovimientoCajaJpaRepository extends JpaRepository<MovimientoCajaEntity, UUID> {

    /**
     * Busca todos los movimientos de caja de una billetera específica.
     * 
     * @param billeteraId El ID de la billetera
     * @return Lista de movimientos ordenados por fecha de creación (más recientes primero)
     */
    @Query("SELECT m FROM MovimientoCajaEntity m WHERE m.billetera.id = :billeteraId ORDER BY m.createdAt DESC")
    List<MovimientoCajaEntity> findByBilleteraIdOrderByCreatedAtDesc(@Param("billeteraId") UUID billeteraId);

    /**
     * Busca todos los movimientos de caja de una billetera específica.
     * 
     * @param billeteraId El ID de la billetera
     * @return Lista de movimientos ordenados por fecha de creación (más antiguos primero)
     */
    @Query("SELECT m FROM MovimientoCajaEntity m WHERE m.billetera.id = :billeteraId ORDER BY m.createdAt ASC")
    List<MovimientoCajaEntity> findByBilleteraIdOrderByCreatedAtAsc(@Param("billeteraId") UUID billeteraId);
}
