package com.budgetpro.domain.finanzas.port.out;

import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado Billetera.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas
 * (JPA, JDBC, etc.). La implementación estará en la capa de infraestructura.
 * 
 * REGLA: Este es un puerto puro del dominio. NO contiene anotaciones JPA/Spring.
 */
public interface BilleteraRepository {

    /**
     * Busca una billetera por el ID del proyecto.
     * 
     * Cada proyecto tiene UNA sola billetera (relación 1:1).
     * 
     * @param proyectoId El ID del proyecto
     * @return Optional con la billetera si existe, vacío en caso contrario
     */
    Optional<Billetera> findByProyectoId(UUID proyectoId);

    /**
     * Guarda una billetera y sus nuevos movimientos de caja.
     * 
     * REGLA CRÍTICA: Este método debe:
     * 1. Persistir la billetera (con el saldo y versión actualizados)
     * 2. Persistir TODOS los movimientos nuevos del agregado
     * 3. Ejecutarse en una transacción ACID única
     * 4. Manejar optimistic locking usando el campo version
     * 
     * Después de persistir exitosamente, debe invocar billetera.limpiarMovimientosNuevos()
     * para limpiar la lista de movimientos pendientes.
     * 
     * @param billetera La billetera a guardar (con sus movimientos nuevos)
     */
    void save(Billetera billetera);

    /**
     * Busca una billetera por su ID.
     * 
     * @param id El ID de la billetera
     * @return Optional con la billetera si existe, vacío en caso contrario
     */
    Optional<Billetera> findById(BilleteraId id);
}
