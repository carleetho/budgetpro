package com.budgetpro.infrastructure.persistence.repository.outbox;

import com.budgetpro.infrastructure.persistence.entity.outbox.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio JPA para la entidad OutboxEventEntity.
 * 
 * Proporciona acceso básico a los datos del Outbox.
 * El consumo/procesamiento de eventos se implementará en tareas futuras.
 */
@Repository
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {
}
