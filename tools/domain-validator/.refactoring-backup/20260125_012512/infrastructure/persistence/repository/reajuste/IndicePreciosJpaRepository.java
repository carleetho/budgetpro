package com.budgetpro.infrastructure.persistence.repository.reajuste;

import com.budgetpro.infrastructure.persistence.entity.reajuste.IndicePreciosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para IndicePreciosEntity.
 */
@Repository
public interface IndicePreciosJpaRepository extends JpaRepository<IndicePreciosEntity, UUID> {

    /**
     * Busca un índice por código y fecha base.
     */
    Optional<IndicePreciosEntity> findByCodigoAndFechaBase(String codigo, LocalDate fechaBase);

    /**
     * Busca el índice más cercano a una fecha para un código dado.
     * Retorna el índice más reciente anterior o igual a la fecha especificada.
     */
    @Query("SELECT i FROM IndicePreciosEntity i WHERE i.codigo = :codigo AND i.fechaBase <= :fecha AND i.activo = true ORDER BY i.fechaBase DESC LIMIT 1")
    Optional<IndicePreciosEntity> findIndiceMasCercano(String codigo, LocalDate fecha);

    /**
     * Busca todos los índices activos de un código dado ordenados por fecha descendente.
     */
    List<IndicePreciosEntity> findByCodigoAndActivoTrueOrderByFechaBaseDesc(String codigo);
}
