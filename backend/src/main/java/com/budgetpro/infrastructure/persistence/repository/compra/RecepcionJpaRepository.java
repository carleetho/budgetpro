package com.budgetpro.infrastructure.persistence.repository.compra;

import com.budgetpro.infrastructure.persistence.entity.compra.RecepcionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para RecepcionEntity.
 */
@Repository
public interface RecepcionJpaRepository extends JpaRepository<RecepcionEntity, UUID> {

    /**
     * Verifica si existe una recepción con el compraId y guiaRemision dados.
     * 
     * Este método se usa para verificar idempotencia al registrar recepciones,
     * asegurando que no se dupliquen recepciones con la misma guía de remisión
     * para la misma compra.
     * 
     * @param compraId El ID de la compra
     * @param guiaRemision El número de guía de remisión
     * @return true si existe una recepción con estos valores, false en caso contrario
     */
    boolean existsByCompraIdAndGuiaRemision(UUID compraId, String guiaRemision);

    /**
     * Busca todas las recepciones de una compra.
     * 
     * Útil para consultar el historial de recepciones de una compra específica.
     * 
     * @param compraId El ID de la compra
     * @return Lista de recepciones de la compra, ordenadas por fecha de recepción
     */
    List<RecepcionEntity> findByCompraIdOrderByFechaRecepcionDesc(UUID compraId);
}
