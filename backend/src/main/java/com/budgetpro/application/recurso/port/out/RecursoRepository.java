package com.budgetpro.application.recurso.port.out;

import com.budgetpro.domain.recurso.model.Recurso;
import com.budgetpro.domain.recurso.model.RecursoId;

import java.util.Optional;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado Recurso.
 * Esta interfaz define el contrato que debe implementar la capa de
 * infraestructura.
 * 
 * Siguiendo los principios de Arquitectura Hexagonal, esta interfaz pertenece a
 * la capa de aplicación y es implementada por adaptadores de infraestructura.
 */
public interface RecursoRepository {

    /**
     * Guarda o actualiza un recurso en el repositorio.
     * 
     * @param recurso El recurso a persistir (no puede ser nulo)
     */
    void save(Recurso recurso);

    /**
     * Busca un recurso por su identificador único.
     * 
     * @param id El identificador del recurso (no puede ser nulo)
     * @return Un Optional que contiene el recurso si existe, o vacío si no se
     *         encuentra
     */
    Optional<Recurso> findById(RecursoId id);

    /**
     * Verifica si existe un recurso con el nombre normalizado dado. Usado para
     * validar la unicidad del nombre en el catálogo.
     * 
     * @param nombreNormalizado El nombre normalizado a verificar (no puede ser nulo
     *                          o vacío)
     * @return true si existe un recurso con ese nombre, false en caso contrario
     */
    boolean existsByNombre(String nombreNormalizado);

    /**
     * Busca un recurso por su nombre normalizado.
     * 
     * @param nombreNormalizado El nombre normalizado a buscar
     * @return Un Optional con el recurso si existe
     */
    Optional<Recurso> findByNombre(String nombreNormalizado);
}
