package com.budgetpro.domain.logistica.inventario.port.out;

import com.budgetpro.domain.logistica.inventario.InventarioItem;
import com.budgetpro.domain.logistica.inventario.InventarioId;
import com.budgetpro.domain.recurso.model.RecursoId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado InventarioItem.
 * 
 * Esta interfaz define el contrato que debe implementar la capa de infraestructura.
 * 
 * Según Directiva Maestra v2.0: Los puertos de salida deben residir en domain/model/{agregado}/port/out
 */
public interface InventarioRepository {

    /**
     * Guarda o actualiza un ítem de inventario en el repositorio.
     * 
     * NOTA: Este método solo funciona para actualizar inventarios existentes.
     * Para crear nuevos inventarios, use save(InventarioItem, UUID proyectoId).
     * 
     * @param inventarioItem El ítem de inventario a persistir (no puede ser nulo)
     * @throws IllegalStateException si se intenta crear un nuevo inventario sin proyectoId
     */
    void save(InventarioItem inventarioItem);

    /**
     * Busca un ítem de inventario por su identificador único.
     * 
     * @param id El identificador del inventario (no puede ser nulo)
     * @return Un Optional que contiene el inventario si existe, o vacío si no se encuentra
     */
    Optional<InventarioItem> findById(InventarioId id);

    /**
     * Busca un ítem de inventario por el ID del recurso.
     * 
     * NOTA: En el modelo actual, puede haber múltiples inventarios por recurso (uno por proyecto).
     * Este método busca el primero encontrado. Para consultas más específicas, se deben usar
     * queries adicionales (ej: por proyecto + recurso).
     * 
     * @param recursoId El ID del recurso
     * @return Un Optional que contiene el inventario si existe, o vacío si no se encuentra
     */
    Optional<InventarioItem> findByRecursoId(RecursoId recursoId);

    /**
     * Busca todos los ítems de inventario para los recursos especificados.
     * 
     * NOTA: Este método busca sin filtro de proyecto. Puede retornar múltiples inventarios
     * por recurso si hay varios proyectos. Use findAllByProyectoIdAndRecursoIds() para búsqueda específica.
     * 
     * @param recursoIds Lista de IDs de recursos
     * @return Mapa con RecursoId como clave e InventarioItem como valor
     */
    Map<RecursoId, InventarioItem> findAllByRecursoIds(List<RecursoId> recursoIds);
    
    /**
     * Busca todos los ítems de inventario para los recursos especificados en un proyecto específico.
     * 
     * Este método es más preciso que findAllByRecursoIds() ya que filtra por proyecto,
     * evitando ambigüedades cuando hay múltiples proyectos con los mismos recursos.
     * 
     * @param proyectoId El ID del proyecto (no puede ser nulo)
     * @param recursoIds Lista de IDs de recursos
     * @return Mapa con RecursoId como clave e InventarioItem como valor
     */
    Map<RecursoId, InventarioItem> findAllByProyectoIdAndRecursoIds(UUID proyectoId, List<RecursoId> recursoIds);
    
    /**
     * Guarda o actualiza un ítem de inventario en el repositorio.
     * 
     * Para crear un nuevo inventario, se requiere proyectoId explícito.
     * 
     * @param inventarioItem El ítem de inventario a persistir (no puede ser nulo)
     * @param proyectoId El ID del proyecto (requerido para creación nueva, debe coincidir para actualización)
     */
    void save(InventarioItem inventarioItem, UUID proyectoId);
}
