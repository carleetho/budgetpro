package com.budgetpro.domain.finanzas.presupuesto.exception;

import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;

import java.util.Objects;

/**
 * Excepción de dominio lanzada cuando se detecta una violación de integridad
 * criptográfica en un presupuesto sellado.
 * 
 * Esta excepción proporciona contexto detallado para:
 * - Logging de seguridad
 * - Análisis forense
 * - Alertas al equipo de seguridad
 * 
 * **Escenarios de uso:**
 * 
 * 1. **Modificación de estructura sellada:**
 *    - Intentar modificar campos estructurales después de aprobación
 *    - Violation type: "Structure modification attempted"
 * 
 * 2. **Tampering detectado:**
 *    - El hash de aprobación recalculado no coincide con el almacenado
 *    - Violation type: "Tampering detected"
 * 
 * 3. **Hash mismatch:**
 *    - El hash calculado difiere del esperado
 *    - Violation type: "Hash mismatch"
 * 
 * **Swiss-Grade Engineering:** Esta excepción protege la invariante crítica
 * de que un presupuesto aprobado no puede ser modificado estructuralmente.
 * 
 * Los hashes se muestran truncados (primeros 16 caracteres) en el mensaje
 * para legibilidad, pero se almacenan completos internamente para análisis forense.
 */
public class BudgetIntegrityViolationException extends RuntimeException {

    private final PresupuestoId presupuestoId;
    private final String expectedHash;
    private final String actualHash;
    private final String violationType;

    /**
     * Constructor para violaciones de integridad detectadas.
     * 
     * @param presupuestoId ID del presupuesto con violación de integridad
     * @param expectedHash Hash esperado (almacenado o calculado previamente)
     * @param actualHash Hash actual (calculado en el momento de la validación)
     * @param violationType Tipo de violación detectada (ej: "Tampering detected", "Structure modification attempted")
     * @throws NullPointerException si presupuestoId es nulo
     */
    public BudgetIntegrityViolationException(
            PresupuestoId presupuestoId,
            String expectedHash,
            String actualHash,
            String violationType
    ) {
        super(formatMessage(presupuestoId, expectedHash, actualHash, violationType));
        this.presupuestoId = Objects.requireNonNull(presupuestoId, "El ID del presupuesto no puede ser nulo");
        this.expectedHash = expectedHash;
        this.actualHash = actualHash;
        this.violationType = violationType;
    }

    /**
     * Formatea el mensaje de la excepción con hashes truncados para legibilidad.
     * Los hashes completos se mantienen en los campos para análisis forense.
     */
    private static String formatMessage(PresupuestoId presupuestoId, String expectedHash,
                                       String actualHash, String violationType) {
        String expectedTruncated = truncateHash(expectedHash);
        String actualTruncated = truncateHash(actualHash);
        
        return String.format(
                "Budget integrity violation detected for %s. Expected hash: %s, Actual hash: %s. Violation: %s",
                presupuestoId.getValue(),
                expectedTruncated,
                actualTruncated,
                violationType
        );
    }

    /**
     * Trunca un hash a los primeros 16 caracteres más "..." para legibilidad.
     * Si el hash es null o tiene menos de 16 caracteres, lo devuelve tal cual.
     */
    private static String truncateHash(String hash) {
        if (hash == null) {
            return "null";
        }
        if (hash.length() <= 16) {
            return hash;
        }
        return hash.substring(0, 16) + "...";
    }

    /**
     * Obtiene el ID del presupuesto con violación de integridad.
     * 
     * @return El ID del presupuesto
     */
    public PresupuestoId getPresupuestoId() {
        return presupuestoId;
    }

    /**
     * Obtiene el hash esperado (almacenado o calculado previamente).
     * Este es el hash que debería coincidir con el actual.
     * 
     * @return El hash esperado completo (64 caracteres hex) o null
     */
    public String getExpectedHash() {
        return expectedHash;
    }

    /**
     * Obtiene el hash actual (calculado en el momento de la validación).
     * Este es el hash que no coincide con el esperado.
     * 
     * @return El hash actual completo (64 caracteres hex) o null
     */
    public String getActualHash() {
        return actualHash;
    }

    /**
     * Obtiene el tipo de violación detectada.
     * 
     * @return Descripción del tipo de violación (ej: "Tampering detected", "Structure modification attempted")
     */
    public String getViolationType() {
        return violationType;
    }
}
