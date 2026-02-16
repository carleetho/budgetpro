package com.budgetpro.infrastructure.persistence.repository.compra;

import com.budgetpro.domain.logistica.compra.model.OrdenCompraEstado;
import com.budgetpro.infrastructure.persistence.entity.compra.OrdenCompraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para OrdenCompraEntity.
 */
@Repository
public interface OrdenCompraJpaRepository extends JpaRepository<OrdenCompraEntity, UUID> {

    /**
     * Busca todas las órdenes de compra de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de órdenes de compra del proyecto
     */
    List<OrdenCompraEntity> findByProyectoId(UUID proyectoId);

    /**
     * Busca todas las órdenes de compra con un estado específico.
     * 
     * @param estado El estado de la orden de compra
     * @return Lista de órdenes de compra con el estado especificado
     */
    List<OrdenCompraEntity> findByEstado(OrdenCompraEstado estado);

    /**
     * Busca todas las órdenes de compra de un proyecto con un estado específico.
     * 
     * @param proyectoId El ID del proyecto
     * @param estado El estado de la orden de compra
     * @return Lista de órdenes de compra del proyecto con el estado especificado
     */
    List<OrdenCompraEntity> findByProyectoIdAndEstado(UUID proyectoId, OrdenCompraEstado estado);

    /**
     * Busca todas las órdenes de compra con número que empiece con el patrón para un año dado.
     * 
     * @param pattern El patrón de búsqueda (ej. "PO-2024-%")
     * @return Lista de órdenes de compra que coinciden con el patrón
     */
    @Query("SELECT o FROM OrdenCompraEntity o WHERE o.numero LIKE :pattern ORDER BY o.numero DESC")
    List<OrdenCompraEntity> findByNumeroPattern(@Param("pattern") String pattern);
}
