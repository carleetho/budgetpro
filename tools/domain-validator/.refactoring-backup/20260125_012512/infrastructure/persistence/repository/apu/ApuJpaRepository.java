package com.budgetpro.infrastructure.persistence.repository.apu;

import com.budgetpro.infrastructure.persistence.entity.apu.ApuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para ApuEntity.
 */
@Repository
public interface ApuJpaRepository extends JpaRepository<ApuEntity, UUID> {

    /**
     * Busca el APU de una partida (relación 1:1).
     * 
     * @param partidaId El ID de la partida
     * @return Optional con el APU si existe
     */
    Optional<ApuEntity> findByPartidaId(UUID partidaId);

    /**
     * Verifica si existe un APU para la partida dada.
     * 
     * @param partidaId El ID de la partida
     * @return true si existe, false en caso contrario
     */
    boolean existsByPartidaId(UUID partidaId);
}
