package com.budgetpro.domain.proyecto.port.out;

import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado Proyecto.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface ProyectoRepository {

    /**
     * Guarda un proyecto.
     * 
     * @param proyecto El proyecto a guardar
     */
    void save(Proyecto proyecto);

    /**
     * Busca un proyecto por su ID.
     * 
     * @param id El ID del proyecto
     * @return Optional con el proyecto si existe, vacío en caso contrario
     */
    Optional<Proyecto> findById(ProyectoId id);

    /**
     * Busca un proyecto por su nombre.
     * 
     * @param nombre El nombre del proyecto (debe estar normalizado)
     * @return Optional con el proyecto si existe, vacío en caso contrario
     */
    Optional<Proyecto> findByNombre(String nombre);

    /**
     * Verifica si existe un proyecto con el nombre dado.
     * 
     * @param nombre El nombre del proyecto (debe estar normalizado)
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Verifica si existe un proyecto con el ID dado.
     * 
     * @param id El ID del proyecto
     * @return true si existe, false en caso contrario
     */
    boolean existsById(ProyectoId id);

    /**
     * Obtiene todos los proyectos.
     *
     * @return Lista de todos los proyectos
     */
    java.util.List<Proyecto> findAll();

    /**
     * Obtiene todos los proyectos que tienen frecuencia de control configurada.
     *
     * @return Lista de proyectos con frecuenciaControl no nulo
     */
    java.util.List<Proyecto> findAllWithFrecuenciaControl();

    /**
     * Indica si el proyecto existe y pertenece al tenant indicado.
     */
    boolean existsByIdAndTenantId(ProyectoId id, java.util.UUID tenantId);
}
