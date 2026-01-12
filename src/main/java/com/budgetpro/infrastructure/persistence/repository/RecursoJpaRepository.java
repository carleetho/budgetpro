package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad RecursoEntity.
 * Proporciona operaciones de persistencia básicas.
 */
@Repository
public interface RecursoJpaRepository extends JpaRepository<RecursoEntity, UUID> {

    /**
     * Verifica si existe un recurso con el nombre normalizado dado.
     * 
     * @param nombreNormalizado El nombre normalizado a buscar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreNormalizado(String nombreNormalizado);

    /**
     * Busca un recurso por su nombre normalizado.
     * 
     * @param nombreNormalizado El nombre normalizado a buscar
     * @return Un Optional con el recurso si existe
     */
    Optional<RecursoEntity> findByNombreNormalizado(String nombreNormalizado);

    /**
     * Busca recursos por nombre (búsqueda difusa, case-insensitive).
     * 
     * Usa LIKE con LOWER para búsqueda parcial en el nombre normalizado.
     * 
     * @param searchQuery El término de búsqueda (se aplica LIKE %searchQuery%)
     * @return Lista de recursos que coinciden con la búsqueda
     */
    @Query("SELECT r FROM RecursoEntity r WHERE LOWER(r.nombreNormalizado) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY r.nombreNormalizado")
    List<RecursoEntity> buscarPorNombre(@Param("search") String searchQuery);

    /**
     * Busca recursos por nombre y tipo (búsqueda difusa, case-insensitive).
     * 
     * @param searchQuery El término de búsqueda
     * @param tipo El tipo de recurso a filtrar
     * @return Lista de recursos que coinciden con la búsqueda y el tipo
     */
    @Query("SELECT r FROM RecursoEntity r WHERE LOWER(r.nombreNormalizado) LIKE LOWER(CONCAT('%', :search, '%')) AND r.tipo = :tipo ORDER BY r.nombreNormalizado")
    List<RecursoEntity> buscarPorNombreYTipo(@Param("search") String searchQuery, @Param("tipo") com.budgetpro.domain.recurso.model.TipoRecurso tipo);
}
