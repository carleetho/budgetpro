package com.budgetpro.infrastructure.persistence.repository.produccion;

import com.budgetpro.infrastructure.persistence.entity.produccion.DetalleRPCEntity;
import com.budgetpro.infrastructure.persistence.entity.produccion.EstadoReporteProduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Repositorio JPA para DetalleRPCEntity.
 */
public interface DetalleRPCJpaRepository extends JpaRepository<DetalleRPCEntity, UUID> {

    @Query("""
        SELECT COALESCE(SUM(d.cantidadReportada), 0)
        FROM DetalleRPCEntity d
        JOIN d.reporteProduccion r
        WHERE d.partida.id = :partidaId
          AND r.estado = :estado
          AND (:reporteId IS NULL OR r.id <> :reporteId)
        """)
    BigDecimal sumarCantidadAprobadaPorPartida(
            @Param("partidaId") UUID partidaId,
            @Param("estado") EstadoReporteProduccion estado,
            @Param("reporteId") UUID reporteId
    );
}
