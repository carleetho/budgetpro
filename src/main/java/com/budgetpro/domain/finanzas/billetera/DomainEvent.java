package com.budgetpro.domain.finanzas.billetera;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Interfaz marker para eventos de dominio.
 * 
 * Todos los eventos de dominio deben implementar esta interfaz.
 * Los eventos son inmutables y representan algo que ocurrió en el dominio.
 */
public interface DomainEvent {
    
    /**
     * ID único del evento.
     */
    UUID getEventId();
    
    /**
     * Timestamp de cuándo ocurrió el evento.
     */
    LocalDateTime getOcurredAt();
}
