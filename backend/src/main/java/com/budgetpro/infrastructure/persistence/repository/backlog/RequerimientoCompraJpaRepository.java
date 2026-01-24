package com.budgetpro.infrastructure.persistence.repository.backlog;

import com.budgetpro.domain.logistica.backlog.model.EstadoRequerimiento;
import com.budgetpro.infrastructure.persistence.entity.backlog.RequerimientoCompraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para RequerimientoCompraEntity.
 */
@Repository
public interface RequerimientoCompraJpaRepository extends JpaRepository<RequerimientoCompraEntity, UUID> {

    /**
     * Busca requerimientos pendientes para un recurso específico.
     * Pendientes = estado PENDIENTE, EN_COTIZACION o ORDENADA.
     */
    @Query("SELECT r FROM RequerimientoCompraEntity r " +
           "WHERE r.proyectoId = :proyectoId " +
           "AND r.recursoExternalId = :recursoExternalId " +
           "AND r.unidadMedida = :unidadMedida " +
           "AND r.estado IN :estadosPendientes")
    List<RequerimientoCompraEntity> findPendientesPorRecurso(
            @Param("proyectoId") UUID proyectoId,
            @Param("recursoExternalId") String recursoExternalId,
            @Param("unidadMedida") String unidadMedida,
            @Param("estadosPendientes") List<EstadoRequerimiento> estadosPendientes
    );

    /**
     * Busca requerimientos por requisición origen.
     */
    List<RequerimientoCompraEntity> findByRequisicionId(UUID requisicionId);
}
