package com.budgetpro.domain.finanzas.presupuesto.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio inmutable para entradas de auditoría de integridad.
 * 
 * Representa un evento de auditoría relacionado con la integridad criptográfica
 * de un presupuesto. Las entradas son inmutables una vez creadas.
 * 
 * **Tipos de eventos:**
 * - HASH_GENERATED: Hash creado al aprobar el presupuesto
 * - HASH_VALIDATED: Hash validado durante una operación
 * - HASH_VIOLATION: Violación de integridad detectada
 * 
 * **Resultados de validación:**
 * - SUCCESS: Hash válido, integridad confirmada
 * - FAILURE: Hash inválido o violación detectada
 * 
 * **Swiss-Grade Engineering:** Este modelo proporciona un registro inmutable
 * y completo de todos los eventos de integridad para análisis forense.
 * 
 * Contexto: Presupuestos & Integridad Criptográfica
 */
public final class IntegrityAuditEntry {

    private final UUID id;
    private final UUID presupuestoId;
    private final String eventType;
    private final String hashApproval;
    private final String hashExecution;
    private final UUID validatedBy;
    private final LocalDateTime validatedAt;
    private final String validationResult;
    private final String violationDetails;
    private final String algorithmVersion;

    /**
     * Constructor privado. Usar factory method crear().
     */
    private IntegrityAuditEntry(UUID id, UUID presupuestoId, String eventType,
                                String hashApproval, String hashExecution,
                                UUID validatedBy, LocalDateTime validatedAt,
                                String validationResult, String violationDetails,
                                String algorithmVersion) {
        this.id = Objects.requireNonNull(id, "El ID de la entrada de auditoría no puede ser nulo");
        this.presupuestoId = Objects.requireNonNull(presupuestoId, "El ID del presupuesto no puede ser nulo");
        this.eventType = Objects.requireNonNull(eventType, "El tipo de evento no puede ser nulo");
        this.validatedAt = Objects.requireNonNull(validatedAt, "La fecha de validación no puede ser nula");
        this.validationResult = Objects.requireNonNull(validationResult, "El resultado de validación no puede ser nulo");
        
        // Campos opcionales (pueden ser null)
        this.hashApproval = hashApproval;
        this.hashExecution = hashExecution;
        this.validatedBy = validatedBy;
        this.violationDetails = violationDetails;
        this.algorithmVersion = algorithmVersion;
        
        validarInvariantes();
    }

    /**
     * Factory method para crear una entrada de auditoría.
     * 
     * @param id ID único de la entrada
     * @param presupuestoId ID del presupuesto auditado
     * @param eventType Tipo de evento (HASH_GENERATED, HASH_VALIDATED, HASH_VIOLATION)
     * @param hashApproval Hash de aprobación (puede ser null)
     * @param hashExecution Hash de ejecución (puede ser null)
     * @param validatedBy ID del usuario que validó (puede ser null para HASH_GENERATED)
     * @param validatedAt Timestamp del evento
     * @param validationResult Resultado (SUCCESS, FAILURE)
     * @param violationDetails Detalles de la violación (puede ser null)
     * @param algorithmVersion Versión del algoritmo (puede ser null)
     * @return Nueva entrada de auditoría inmutable
     * @throws IllegalArgumentException si los parámetros no son válidos
     */
    public static IntegrityAuditEntry crear(UUID id, UUID presupuestoId, String eventType,
                                           String hashApproval, String hashExecution,
                                           UUID validatedBy, LocalDateTime validatedAt,
                                           String validationResult, String violationDetails,
                                           String algorithmVersion) {
        return new IntegrityAuditEntry(id, presupuestoId, eventType, hashApproval, hashExecution,
                                       validatedBy, validatedAt, validationResult, violationDetails,
                                       algorithmVersion);
    }

    /**
     * Valida las invariantes del modelo.
     */
    private void validarInvariantes() {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("El tipo de evento no puede estar vacío");
        }
        
        if (!eventType.equals("HASH_GENERATED") && 
            !eventType.equals("HASH_VALIDATED") && 
            !eventType.equals("HASH_VIOLATION")) {
            throw new IllegalArgumentException(
                String.format("Tipo de evento inválido: %s. Debe ser HASH_GENERATED, HASH_VALIDATED o HASH_VIOLATION", eventType)
            );
        }
        
        if (!validationResult.equals("SUCCESS") && !validationResult.equals("FAILURE")) {
            throw new IllegalArgumentException(
                String.format("Resultado de validación inválido: %s. Debe ser SUCCESS o FAILURE", validationResult)
            );
        }
        
        // Validar formato de hash si está presente y no está vacío
        // Nota: Para violaciones (HASH_VIOLATION), los hashes pueden tener formatos diferentes
        // o ser null, así que solo validamos longitud para HASH_GENERATED y HASH_VALIDATED
        if (hashApproval != null && !hashApproval.isBlank() && 
            !"HASH_VIOLATION".equals(eventType) && hashApproval.length() != 64) {
            throw new IllegalArgumentException(
                String.format("Hash de aprobación debe tener 64 caracteres hexadecimales, encontrado: %d", hashApproval.length())
            );
        }
        
        if (hashExecution != null && !hashExecution.isBlank() && 
            !"HASH_VIOLATION".equals(eventType) && hashExecution.length() != 64) {
            throw new IllegalArgumentException(
                String.format("Hash de ejecución debe tener 64 caracteres hexadecimales, encontrado: %d", hashExecution.length())
            );
        }
    }

    // Getters (inmutabilidad garantizada por final fields)

    public UUID getId() {
        return id;
    }

    public UUID getPresupuestoId() {
        return presupuestoId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getHashApproval() {
        return hashApproval;
    }

    public String getHashExecution() {
        return hashExecution;
    }

    public UUID getValidatedBy() {
        return validatedBy;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public String getValidationResult() {
        return validationResult;
    }

    public String getViolationDetails() {
        return violationDetails;
    }

    public String getAlgorithmVersion() {
        return algorithmVersion;
    }

    /**
     * Verifica si esta entrada representa una violación de integridad.
     */
    public boolean isViolation() {
        return "HASH_VIOLATION".equals(eventType);
    }

    /**
     * Verifica si la validación fue exitosa.
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(validationResult);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegrityAuditEntry that = (IntegrityAuditEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
            "IntegrityAuditEntry{id=%s, presupuestoId=%s, eventType='%s', validationResult='%s', validatedAt=%s}",
            id, presupuestoId, eventType, validationResult, validatedAt
        );
    }
}
