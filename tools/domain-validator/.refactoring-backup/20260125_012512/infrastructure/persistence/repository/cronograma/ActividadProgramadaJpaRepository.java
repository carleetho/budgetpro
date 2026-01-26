package com.budgetpro.infrastructure.persistence.repository.cronograma;

import com.budgetpro.infrastructure.persistence.entity.cronograma.ActividadProgramadaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para ActividadProgramadaEntity.
 */
@Repository
public interface ActividadProgramadaJpaRepository extends JpaRepository<ActividadProgramadaEntity, UUID> {

    /**
     * Busca todas las actividades de un programa de obra.
     * 
     * @param programaObraId El ID del programa de obra
     * @return Lista de actividades del programa
     */
    List<ActividadProgramadaEntity> findByProgramaObraId(UUID programaObraId);

    /**
     * Busca la actividad programada de una partida (relación 1:1).
     * 
     * @param partidaId El ID de la partida
     * @return Optional con la actividad si existe
     */
    Optional<ActividadProgramadaEntity> findByPartidaId(UUID partidaId);
}
