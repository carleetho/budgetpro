package com.budgetpro.domain.logistica.compra.port.out;

import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.domain.logistica.compra.model.Recepcion;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado Recepcion.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface RecepcionRepository {

    /**
     * Guarda una recepción y todos sus detalles.
     * 
     * @param recepcion La recepción a guardar (con su lista de detalles)
     */
    void save(Recepcion recepcion);

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
    boolean existsByCompraIdAndGuiaRemision(CompraId compraId, String guiaRemision);
}
