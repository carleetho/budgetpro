package com.budgetpro.infrastructure.persistence.adapter.outbox;

import com.budgetpro.domain.finanzas.compra.port.out.OutboxEventRepository;
import com.budgetpro.domain.finanzas.compra.event.CompraRegistradaEvent;
import com.budgetpro.infrastructure.persistence.entity.outbox.OutboxEventEntity;
import com.budgetpro.infrastructure.persistence.repository.outbox.OutboxEventJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adaptador de persistencia para el Outbox de eventos.
 * 
 * Implementa el patrón Outbox persistiendo eventos de dominio dentro de la misma
 * transacción que los cambios de negocio, garantizando atomicidad.
 * 
 * Responsabilidades:
 * - Serializar eventos de dominio a JSON
 * - Persistir eventos en la tabla outbox_event
 * - Mantener el dominio limpio de infraestructura
 */
@Component
public class OutboxEventRepositoryAdapter implements OutboxEventRepository {

    private final OutboxEventJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventRepositoryAdapter(
            OutboxEventJpaRepository jpaRepository,
            ObjectMapper objectMapper
    ) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(CompraRegistradaEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo");
        }

        try {
            // Serializar el evento a JSON
            String eventPayload = objectMapper.writeValueAsString(event);

            // Crear entidad JPA
            OutboxEventEntity entity = new OutboxEventEntity(
                UUID.randomUUID(),
                "Compra",
                event.compraId(),
                "CompraRegistrada",
                eventPayload
            );

            // Persistir en la misma transacción
            jpaRepository.save(entity);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                String.format("Error al serializar evento de compra %s", event.compraId()),
                e
            );
        }
    }
}
