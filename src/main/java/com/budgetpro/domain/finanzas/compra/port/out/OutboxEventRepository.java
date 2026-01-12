package com.budgetpro.domain.finanzas.compra.port.out;

import com.budgetpro.domain.finanzas.compra.event.CompraRegistradaEvent;

/**
 * Puerto de Salida (Outbound Port) para persistir eventos de dominio en el Outbox.
 * 
 * Esta interfaz define el contrato que debe implementar la capa de infraestructura
 * para persistir eventos dentro de la misma transacción que los cambios de negocio.
 * 
 * Según Directiva Maestra v2.0: Los puertos de salida deben residir en domain/model/{agregado}/port/out
 */
public interface OutboxEventRepository {

    /**
     * Persiste un evento de compra registrada en el Outbox.
     * 
     * Este método debe ejecutarse dentro de la misma transacción que la persistencia
     * de la compra para garantizar atomicidad.
     * 
     * @param event El evento de compra registrada a persistir (no puede ser nulo)
     */
    void save(CompraRegistradaEvent event);
}
