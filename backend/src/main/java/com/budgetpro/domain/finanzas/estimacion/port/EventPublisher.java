package com.budgetpro.domain.finanzas.estimacion.port;

/**
 * Puerto de salida para publicar eventos de dominio de la estimación. Permite
 * desacoplar el dominio del mecanismo de publicación (e.g. Spring events).
 */
public interface EventPublisher {

    /**
     * Publica cualquier objeto como evento.
     * 
     * @param event El evento de dominio a publicar.
     */
    void publish(Object event);
}
