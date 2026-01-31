package com.budgetpro.infrastructure.persistence.repository.estimacion;

import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para EstimacionEntity.
 */
@Repository
public interface EstimacionJpaRepository extends JpaRepository<EstimacionEntity, UUID> {

    /**
     * Busca todas las estimaciones de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de estimaciones del proyecto
     */
    List<EstimacionEntity> findByProyectoIdOrderByNumeroEstimacionAsc(UUID proyectoId);

    /**
     * Busca todas las estimaciones aprobadas de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de estimaciones aprobadas
     */
    @Query("SELECT e FROM EstimacionEntity e WHERE e.proyectoId = :proyectoId AND e.estado = 'APROBADA' ORDER BY e.numeroEstimacion ASC")
    List<EstimacionEntity> findAprobadasByProyectoId(UUID proyectoId);

    /**
     * Obtiene el siguiente número de estimación para un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return El número de la última estimación + 1, o 1 si no hay estimaciones
     *         previas
     */
    @Query("SELECT COALESCE(MAX(e.numeroEstimacion), 0) + 1 FROM EstimacionEntity e WHERE e.proyectoId = :proyectoId")
    Integer obtenerSiguienteNumeroEstimacion(UUID proyectoId);

    /**
     * Verifica si existe un periodo solapado para un proyecto.
     * 
     * @param proyectoId  El ID del proyecto
     * @param fechaInicio Fecha de inicio del periodo
     * @param fechaFin    Fecha de fin del periodo
     * @return true si existe solapamiento, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM EstimacionEntity e "
            + "WHERE e.proyectoId = :proyectoId "
            + "AND ((e.periodoInicio <= :fechaFin AND e.periodoFin >= :fechaInicio))")
    boolean existsPeriodoSolapado(@Param("proyectoId") UUID proyectoId, @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Verifica si existe un periodo solapado para un proyecto, excluyendo una
     * estimación específica.
     * 
     * @param proyectoId  El ID del proyecto
     * @param fechaInicio Fecha de inicio del periodo
     * @param fechaFin    Fecha de fin del periodo
     * @param excludeId   ID de la estimación a excluir de la verificación
     * @return true si existe solapamiento, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM EstimacionEntity e "
            + "WHERE e.proyectoId = :proyectoId " + "AND e.id != :excludeId "
            + "AND ((e.periodoInicio <= :fechaFin AND e.periodoFin >= :fechaInicio))")
    boolean existsPeriodoSolapadoExcludingId(@Param("proyectoId") UUID proyectoId,
            @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin,
            @Param("excludeId") UUID excludeId);
}
