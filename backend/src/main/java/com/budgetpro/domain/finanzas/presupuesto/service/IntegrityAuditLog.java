package com.budgetpro.domain.finanzas.presupuesto.service;

import com.budgetpro.domain.finanzas.presupuesto.exception.BudgetIntegrityViolationException;
import com.budgetpro.domain.finanzas.presupuesto.model.IntegrityAuditEntry;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.port.out.IntegrityAuditRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio de dominio para registro de eventos de auditoría de integridad.
 * 
 * Proporciona métodos para registrar todos los eventos relacionados con la
 * integridad criptográfica de presupuestos:
 * 
 * - Generación de hashes al aprobar presupuestos - Validación de hashes durante
 * operaciones críticas - Violaciones de integridad detectadas
 * 
 * **Patrón Fire-and-Forget:** Los métodos de logging son asíncronos y no
 * bloquean el flujo principal. Si el logging falla, no debe afectar la
 * operación principal.
 * 
 * **Swiss-Grade Engineering:** Este servicio crea un registro inmutable y
 * completo de todos los eventos de integridad para análisis forense y
 * cumplimiento normativo.
 * 
 * **Integración con Seguridad:** Las violaciones de integridad deben disparar
 * alertas al equipo de seguridad. Esta integración se implementará en una tarea
 * separada.
 * 
 * Contexto: Presupuestos & Integridad Criptográfica
 */
public class IntegrityAuditLog {

    private final IntegrityAuditRepository auditRepository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param auditRepository Repositorio para persistir entradas de auditoría
     */
    public IntegrityAuditLog(IntegrityAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Registra la generación de hashes de integridad al aprobar un presupuesto.
     * 
     * Este evento se registra cuando un presupuesto es aprobado y se generan los
     * hashes criptográficos de aprobación y ejecución.
     * 
     * @param presupuesto Presupuesto que fue aprobado y sellado criptográficamente
     * @throws IllegalArgumentException si el presupuesto es null o no tiene hashes
     */
    public void logHashGeneration(Presupuesto presupuesto) {
        if (presupuesto == null) {
            throw new IllegalArgumentException("El presupuesto no puede ser nulo");
        }

        if (presupuesto.getIntegrityHashApproval() == null) {
            throw new IllegalArgumentException(
                    "No se puede registrar generación de hash para un presupuesto sin hash de aprobación");
        }

        IntegrityAuditEntry entry = IntegrityAuditEntry.crear(UUID.randomUUID(), presupuesto.getId().getValue(),
                "HASH_GENERATED", presupuesto.getIntegrityHashApproval(), presupuesto.getIntegrityHashExecution(),
                presupuesto.getIntegrityHashGeneratedBy(),
                presupuesto.getIntegrityHashGeneratedAt() != null ? presupuesto.getIntegrityHashGeneratedAt()
                        : LocalDateTime.now(),
                "SUCCESS", null, presupuesto.getIntegrityHashAlgorithm());

        auditRepository.save(entry);
    }

    /**
     * Registra la validación de hashes de integridad durante una operación.
     * 
     * Este evento se registra cuando se valida la integridad de un presupuesto
     * antes de permitir una operación crítica (ej: aprobar compra, realizar
     * egreso).
     * 
     * @param presupuesto Presupuesto que fue validado
     * @param validatedBy ID del usuario que realizó la validación
     * @param success     true si la validación fue exitosa, false si falló
     * @param details     Detalles adicionales de la validación (puede ser null)
     */
    public void logHashValidation(Presupuesto presupuesto, UUID validatedBy, boolean success, String details) {
        if (presupuesto == null) {
            throw new IllegalArgumentException("El presupuesto no puede ser nulo");
        }

        IntegrityAuditEntry entry = IntegrityAuditEntry.crear(UUID.randomUUID(), presupuesto.getId().getValue(),
                "HASH_VALIDATED", presupuesto.getIntegrityHashApproval(), presupuesto.getIntegrityHashExecution(),
                validatedBy, LocalDateTime.now(), success ? "SUCCESS" : "FAILURE", details,
                presupuesto.getIntegrityHashAlgorithm());

        auditRepository.save(entry);
    }

    /**
     * Registra una violación de integridad detectada.
     * 
     * CRÍTICO: Este método registra violaciones de integridad criptográfica. Las
     * violaciones indican posibles intentos de tampering o modificaciones no
     * autorizadas a presupuestos sellados.
     * 
     * Este método debe: 1. Registrar la violación en el audit log 2. Disparar una
     * alerta al equipo de seguridad (implementación futura)
     * 
     * @param exception  Excepción de violación de integridad con contexto completo
     * @param detectedBy ID del usuario o sistema que detectó la violación
     */
    public void logIntegrityViolation(BudgetIntegrityViolationException exception, UUID detectedBy) {
        if (exception == null) {
            throw new IllegalArgumentException("La excepción de violación no puede ser nula");
        }

        IntegrityAuditEntry entry = IntegrityAuditEntry.crear(UUID.randomUUID(),
                exception.getPresupuestoId().getValue(), "HASH_VIOLATION", exception.getExpectedHash(),
                exception.getActualHash(), detectedBy, LocalDateTime.now(), "FAILURE", exception.getViolationType(),
                "SHA-256-v1" // Usar versión del algoritmo actual
        );

        auditRepository.save(entry);

        // TODO: Integrar con servicio de alertas de seguridad
        // securityAlertService.notifyIntegrityViolation(exception);
    }
}
