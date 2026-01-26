package com.budgetpro.domain.logistica.requisicion.port.out;

import com.budgetpro.domain.logistica.requisicion.model.Requisicion;
import com.budgetpro.domain.logistica.requisicion.model.RequisicionId;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado Requisicion.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas
 * (JPA, JDBC, etc.). La implementación estará en la capa de infraestructura.
 * 
 * REGLA: Este es un puerto puro del dominio. NO contiene anotaciones JPA/Spring.
 */
public interface RequisicionRepository {

    /**
     * Busca una requisición por su ID.
     * 
     * @param id El ID de la requisición
     * @return Optional con la requisición si existe, vacío en caso contrario
     */
    Optional<Requisicion> findById(RequisicionId id);

    /**
     * Guarda una requisición y sus ítems.
     * 
     * REGLA CRÍTICA: Este método debe:
     * 1. Persistir la requisición (con estado, versión actualizados)
     * 2. Persistir TODOS los ítems de la requisición
     * 3. Ejecutarse en una transacción ACID única
     * 4. Manejar optimistic locking usando el campo version
     * 
     * @param requisicion La requisición a guardar (con sus ítems)
     */
    void save(Requisicion requisicion);

    /**
     * Busca todas las requisiciones de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de requisiciones del proyecto
     */
    java.util.List<Requisicion> findByProyectoId(UUID proyectoId);
}
