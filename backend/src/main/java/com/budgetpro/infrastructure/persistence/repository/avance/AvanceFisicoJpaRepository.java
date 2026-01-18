package com.budgetpro.infrastructure.persistence.repository.avance;

import com.budgetpro.infrastructure.persistence.entity.avance.AvanceFisicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para AvanceFisicoEntity.
 */
@Repository
public interface AvanceFisicoJpaRepository extends JpaRepository<AvanceFisicoEntity, UUID> {

    /**
     * Busca todos los avances de una partida.
     * 
     * @param partidaId El ID de la partida
     * @return Lista de avances de la partida
     */
    List<AvanceFisicoEntity> findByPartidaId(UUID partidaId);

    /**
     * Busca todos los avances de una partida en un rango de fechas.
     * 
     * @param partidaId El ID de la partida
     * @param fechaInicio Fecha de inicio (inclusive)
     * @param fechaFin Fecha de fin (inclusive)
     * @return Lista de avances en el rango
     */
    List<AvanceFisicoEntity> findByPartidaIdAndFechaBetween(UUID partidaId, LocalDate fechaInicio, LocalDate fechaFin);
}
