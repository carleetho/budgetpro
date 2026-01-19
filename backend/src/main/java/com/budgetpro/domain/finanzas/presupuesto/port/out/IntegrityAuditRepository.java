package com.budgetpro.domain.finanzas.presupuesto.port.out;

import com.budgetpro.domain.finanzas.presupuesto.model.IntegrityAuditEntry;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida (outbound port) para persistencia de entradas de auditoría de integridad.
 * 
 * Define el contrato para almacenar y consultar eventos de auditoría de integridad
 * criptográfica de presupuestos.
 * 
 * **Responsabilidades:**
 * - Persistir entradas de auditoría inmutables
 * - Consultar eventos por presupuesto
 * - Consultar violaciones de integridad
 * 
 * **Patrón Hexagonal:** Este puerto define la interfaz que será implementada
 * por un adaptador de persistencia en la capa de infraestructura.
 * 
 * Contexto: Presupuestos & Integridad Criptográfica
 */
public interface IntegrityAuditRepository {

    /**
     * Guarda una entrada de auditoría de integridad.
     * 
     * Las entradas son inmutables y no pueden ser modificadas después de la creación.
     * 
     * @param entry Entrada de auditoría a persistir
     * @return Entrada persistida (puede tener campos adicionales como created_at)
     * @throws IllegalArgumentException si la entrada es null
     */
    IntegrityAuditEntry save(IntegrityAuditEntry entry);

    /**
     * Busca todas las entradas de auditoría para un presupuesto específico.
     * 
     * Útil para análisis forense de un presupuesto en particular.
     * 
     * @param presupuestoId ID del presupuesto
     * @return Lista de entradas de auditoría ordenadas por fecha (más reciente primero)
     */
    List<IntegrityAuditEntry> findByPresupuestoId(UUID presupuestoId);

    /**
     * Busca todas las violaciones de integridad detectadas.
     * 
     * CRÍTICO: Este método es usado por el equipo de seguridad para monitorear
     * intentos de tampering o modificaciones no autorizadas.
     * 
     * @return Lista de entradas de auditoría con eventType = HASH_VIOLATION,
     *         ordenadas por fecha (más reciente primero)
     */
    List<IntegrityAuditEntry> findViolations();
}
