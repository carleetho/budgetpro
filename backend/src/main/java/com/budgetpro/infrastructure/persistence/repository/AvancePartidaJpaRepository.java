package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.estimacion.AvancePartidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface AvancePartidaJpaRepository extends JpaRepository<AvancePartidaEntity, UUID> {

    List<AvancePartidaEntity> findByPartidaId(UUID partidaId);

    @Query("SELECT SUM(a.porcentajeAvance) FROM AvancePartidaEntity a WHERE a.partidaId = :partidaId")
    BigDecimal sumPorcentajeAvanceByPartidaId(@Param("partidaId") UUID partidaId);

    @Query("SELECT SUM(a.montoAcumulado) FROM AvancePartidaEntity a WHERE a.partidaId = :partidaId")
    BigDecimal sumMontoAcumuladoByPartidaId(@Param("partidaId") UUID partidaId);
}
