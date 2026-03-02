package com.budgetpro.infrastructure.persistence.repository.evm;

import com.budgetpro.infrastructure.persistence.entity.evm.EVMTimeSeriesEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para EVMTimeSeriesEntity.
 */
@Repository
public interface JpaEVMTimeSeriesRepository extends JpaRepository<EVMTimeSeriesEntity, UUID> {

    /**
     * Consulta de solo lectura para obtener la última serie de un proyecto.
     */
    Optional<EVMTimeSeriesEntity> findFirstByProyectoIdOrderByFechaCorteDesc(UUID proyectoId);

    /**
     * Obtiene la última serie temporal con lock pesimista de escritura.
     *
     * La combinación {@code @Lock(PESSIMISTIC_WRITE)} + query ordenada permite que Hibernate
     * emita SQL con {@code FOR UPDATE} en transacción activa.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT e
            FROM EVMTimeSeriesEntity e
            WHERE e.proyectoId = :proyectoId
              AND e.fechaCorte = (
                  SELECT MAX(e2.fechaCorte)
                  FROM EVMTimeSeriesEntity e2
                  WHERE e2.proyectoId = :proyectoId
              )
            """)
    Optional<EVMTimeSeriesEntity> findLatestByProyectoIdWithLock(@Param("proyectoId") UUID proyectoId);

    /**
     * Consulta por rango de fechas ascendente; start/end null se interpretan como no acotados.
     */
    @Query("""
            SELECT e
            FROM EVMTimeSeriesEntity e
            WHERE e.proyectoId = :proyectoId
              AND (:startDate IS NULL OR e.fechaCorte >= :startDate)
              AND (:endDate IS NULL OR e.fechaCorte <= :endDate)
            ORDER BY e.fechaCorte ASC, e.periodo ASC
            """)
    List<EVMTimeSeriesEntity> findByProyectoIdAndFechaCorteRangeOrderByFechaCorteAsc(
            @Param("proyectoId") UUID proyectoId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}

