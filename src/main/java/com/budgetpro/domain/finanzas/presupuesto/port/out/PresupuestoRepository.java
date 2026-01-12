package com.budgetpro.domain.finanzas.presupuesto.port.out;

import com.budgetpro.domain.finanzas.presupuesto.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.PresupuestoId;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado Presupuesto.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas
 * (JPA, JDBC, etc.). La implementación estará en la capa de infraestructura.
 * 
 * REGLA: Este es un puerto puro del dominio. NO contiene anotaciones JPA/Spring.
 * 
 * NOTA: Este repositorio persiste el agregado Presupuesto y sus Partida internas.
 * Las Partida son entidades internas del agregado y se persisten como parte del agregado raíz.
 */
public interface PresupuestoRepository {

    /**
     * Guarda un presupuesto (creación o actualización).
     * 
     * Debe manejar optimistic locking usando el campo version.
     * Debe persistir el presupuesto y todas sus partidas internas.
     * 
     * @param presupuesto El presupuesto a guardar (con sus partidas)
     */
    void save(Presupuesto presupuesto);

    /**
     * Busca un presupuesto por su ID.
     * 
     * Debe reconstruir el agregado completo incluyendo todas sus partidas.
     * 
     * @param id El ID del presupuesto
     * @return Optional con el presupuesto si existe, vacío en caso contrario
     */
    Optional<Presupuesto> findById(PresupuestoId id);

    /**
     * Busca un presupuesto por el ID del proyecto.
     * 
     * NOTA: Puede haber múltiples presupuestos por proyecto. Este método busca el primero encontrado.
     * Para consultas más específicas (ej: presupuesto contractual), se debe usar queries adicionales.
     * 
     * @param proyectoId El ID del proyecto
     * @return Optional con el presupuesto si existe, vacío en caso contrario
     */
    Optional<Presupuesto> findByProyectoId(UUID proyectoId);

    /**
     * Verifica si existe un presupuesto con el ID especificado.
     * 
     * @param id El ID del presupuesto
     * @return true si existe, false en caso contrario
     */
    boolean existsById(PresupuestoId id);
}
