package com.budgetpro.domain.finanzas.cronograma.exception;

import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;

import java.util.Objects;

/**
 * Excepción de dominio lanzada cuando se intenta modificar un cronograma
 * que ha sido congelado (baseline establecido).
 * 
 * Esta excepción protege la invariante crítica de que un cronograma congelado
 * no puede ser modificado después de establecer el baseline.
 * 
 * **Escenarios de uso:**
 * 
 * 1. **Modificación de fechas en cronograma congelado:**
 *    - Intentar actualizar fechaInicio o fechaFinEstimada después del congelamiento
 *    - Intentar actualizar fechaFinDesdeActividades después del congelamiento
 * 
 * **Patrón de Freeze:**
 * Una vez que un cronograma es congelado mediante el método congelar(),
 * todas las operaciones de modificación de fechas quedan bloqueadas para
 * preservar la integridad del baseline establecido.
 */
public class CronogramaCongeladoException extends RuntimeException {

    private final ProgramaObraId programaObraId;
    private final String operacionIntentada;

    /**
     * Constructor para violaciones de congelamiento detectadas.
     * 
     * @param programaObraId ID del programa de obra congelado
     * @param operacionIntentada Descripción de la operación que se intentó realizar
     * @throws NullPointerException si programaObraId es nulo
     */
    public CronogramaCongeladoException(ProgramaObraId programaObraId, String operacionIntentada) {
        super(formatMessage(programaObraId, operacionIntentada));
        this.programaObraId = Objects.requireNonNull(programaObraId, "El ID del programa de obra no puede ser nulo");
        this.operacionIntentada = operacionIntentada;
    }

    /**
     * Formatea el mensaje de la excepción con información detallada.
     */
    private static String formatMessage(ProgramaObraId programaObraId, String operacionIntentada) {
        return String.format(
                "No se puede modificar el cronograma %s porque está congelado. Operación intentada: %s",
                programaObraId.getValue(),
                operacionIntentada != null ? operacionIntentada : "modificación de fechas"
        );
    }

    /**
     * Obtiene el ID del programa de obra congelado.
     * 
     * @return El ID del programa de obra
     */
    public ProgramaObraId getProgramaObraId() {
        return programaObraId;
    }

    /**
     * Obtiene la descripción de la operación que se intentó realizar.
     * 
     * @return Descripción de la operación intentada
     */
    public String getOperacionIntentada() {
        return operacionIntentada;
    }
}
