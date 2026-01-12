-- Migración Flyway V5: Crear tabla outbox_event
-- Patrón Outbox para garantizar persistencia confiable de eventos de dominio

-- Crear tabla outbox_event
-- Almacena eventos de dominio para publicación asíncrona posterior
CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    processed_at TIMESTAMP,
    
    CONSTRAINT chk_outbox_event_status CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED'))
);

-- Índices para mejorar rendimiento de consultas del poller
CREATE INDEX idx_outbox_event_status ON outbox_event(status);
CREATE INDEX idx_outbox_event_created_at ON outbox_event(created_at);
CREATE INDEX idx_outbox_event_aggregate ON outbox_event(aggregate_type, aggregate_id);
