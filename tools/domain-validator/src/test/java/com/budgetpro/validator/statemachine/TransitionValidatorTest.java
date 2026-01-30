package com.budgetpro.validator.statemachine;

import com.budgetpro.validator.analyzer.StateMachineConfig;
import com.budgetpro.validator.model.StateAssignment;
import com.budgetpro.validator.model.TransitionViolation;
import com.budgetpro.validator.model.ViolationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para TransitionValidator.
 */
class TransitionValidatorTest {
    
    private TransitionValidator validator;
    private StateMachineConfig config;
    
    @BeforeEach
    void setUp() {
        validator = new TransitionValidator();
        
        // Configurar máquinas de estado
        Map<String, List<String>> stateMachines = new HashMap<>();
        stateMachines.put("EstadoPresupuesto", 
                List.of("BORRADOR", "CONGELADO", "INVALIDADO"));
        
        // Configurar transiciones válidas para Presupuesto
        Map<String, Map<String, List<String>>> transitions = new HashMap<>();
        Map<String, List<String>> presupuestoTransitions = new HashMap<>();
        presupuestoTransitions.put("BORRADOR", List.of("CONGELADO"));
        presupuestoTransitions.put("CONGELADO", List.of("INVALIDADO"));
        transitions.put("Presupuesto", presupuestoTransitions);
        
        // Configurar estados finales
        Map<String, Set<String>> finalStates = new HashMap<>();
        finalStates.put("Presupuesto", Set.of("CONGELADO"));
        
        config = new StateMachineConfig(stateMachines, transitions, finalStates);
    }
    
    @Test
    void deberiaPasarTransicionValida() {
        // Setup: Transición válida BORRADOR → CONGELADO
        StateAssignment assignment = new StateAssignment(
                "Presupuesto.java",
                10,
                "aprobar",
                "BORRADOR",
                "CONGELADO",
                "estado",
                "Presupuesto"
        );
        assignment.setValid(true);
        
        // Action
        List<TransitionViolation> violations = validator.validate(List.of(assignment), config);
        
        // Expect
        assertTrue(violations.isEmpty(), 
                "Transición válida no debe generar violaciones");
    }
    
    @Test
    void deberiaDetectarTransicionInvalida() {
        // Setup: Transición inválida BORRADOR → INVALIDADO (no permitida, solo BORRADOR → CONGELADO)
        StateAssignment assignment = new StateAssignment(
                "Presupuesto.java",
                15,
                "invalidar",
                "BORRADOR",
                "INVALIDADO",
                "estado",
                "Presupuesto"
        );
        assignment.setValid(true);
        
        // Action
        List<TransitionViolation> violations = validator.validate(List.of(assignment), config);
        
        // Expect
        assertEquals(1, violations.size(), "Debe detectar una violación");
        
        TransitionViolation violation = violations.get(0);
        assertEquals(ViolationSeverity.CRITICAL, violation.getSeverity());
        assertEquals(TransitionViolation.ViolationType.INVALID_TRANSITION, violation.getViolationType());
        assertEquals("BORRADOR", violation.getFromState());
        assertEquals("INVALIDADO", violation.getToState());
        assertTrue(violation.getMessage().contains("Transición inválida"), 
                "Mensaje: " + violation.getMessage());
        assertTrue(violation.getMessage().contains("BORRADOR") && 
                   violation.getMessage().contains("INVALIDADO"),
                "Mensaje debe mencionar BORRADOR e INVALIDADO. Mensaje: " + violation.getMessage());
    }
    
    @Test
    void deberiaDetectarTransicionDesdeEstadoFinal() {
        // Setup: Intento de transición desde CONGELADO (estado final)
        StateAssignment assignment = new StateAssignment(
                "Presupuesto.java",
                20,
                "modificar",
                "CONGELADO",
                "BORRADOR",
                "estado",
                "Presupuesto"
        );
        assignment.setValid(true);
        
        // Action
        List<TransitionViolation> violations = validator.validate(List.of(assignment), config);
        
        // Expect
        assertEquals(1, violations.size(), "Debe detectar violación de estado final");
        
        TransitionViolation violation = violations.get(0);
        assertEquals(ViolationSeverity.CRITICAL, violation.getSeverity());
        assertEquals(TransitionViolation.ViolationType.INVALID_TRANSITION, violation.getViolationType());
        assertEquals("CONGELADO", violation.getFromState());
        assertTrue(violation.getMessage().contains("estado final"));
        assertTrue(violation.getMessage().contains("no permite transiciones"));
        assertEquals(List.of(), violation.getValidTransitions(), 
                "Estados finales no tienen transiciones válidas");
    }
    
    @Test
    void deberiaGenerarAdvertenciaPorValidacionFaltante() {
        // Setup: Asignación sin estado origen (fromState = null)
        StateAssignment assignment = new StateAssignment(
                "Presupuesto.java",
                25,
                "cambiarEstado",
                null,  // fromState no determinable
                "CONGELADO",
                "estado",
                "Presupuesto"
        );
        assignment.setValid(true);
        
        // Action
        List<TransitionViolation> violations = validator.validate(List.of(assignment), config);
        
        // Expect
        assertEquals(1, violations.size(), "Debe detectar falta de validación");
        
        TransitionViolation violation = violations.get(0);
        assertEquals(ViolationSeverity.WARNING, violation.getSeverity());
        assertEquals(TransitionViolation.ViolationType.MISSING_VALIDATION, violation.getViolationType());
        assertNull(violation.getFromState());
        assertEquals("CONGELADO", violation.getToState());
        assertTrue(violation.getMessage().contains("Falta lógica de validación"));
        assertTrue(violation.getMessage().contains("validación condicional"));
    }
    
    @Test
    void deberiaDetectarEstadoNoExistente() {
        // Setup: Estado destino no existe en el enum
        StateAssignment assignment = new StateAssignment(
                "Presupuesto.java",
                30,
                "setEstado",
                "BORRADOR",
                "INVALID_STATE",
                "estado",
                "Presupuesto"
        );
        assignment.setValid(false);  // Estado no válido
        
        // Action
        List<TransitionViolation> violations = validator.validate(List.of(assignment), config);
        
        // Expect
        assertEquals(1, violations.size(), "Debe detectar estado no existente");
        
        TransitionViolation violation = violations.get(0);
        assertEquals(ViolationSeverity.CRITICAL, violation.getSeverity());
        assertEquals(TransitionViolation.ViolationType.NON_EXISTENT_STATE, violation.getViolationType());
        assertEquals("INVALID_STATE", violation.getToState());
        assertTrue(violation.getMessage().contains("no existe en el enum"));
    }
    
    @Test
    void deberiaValidarMultiplesAsignaciones() {
        // Setup: Múltiples asignaciones, algunas válidas y otras inválidas
        StateAssignment validAssignment = new StateAssignment(
                "Presupuesto.java",
                10,
                "aprobar",
                "BORRADOR",
                "CONGELADO",
                "estado",
                "Presupuesto"
        );
        validAssignment.setValid(true);
        
        StateAssignment invalidAssignment = new StateAssignment(
                "Presupuesto.java",
                20,
                "revertir",
                "CONGELADO",
                "BORRADOR",
                "estado",
                "Presupuesto"
        );
        invalidAssignment.setValid(true);
        
        StateAssignment missingValidation = new StateAssignment(
                "Presupuesto.java",
                30,
                "cambiar",
                null,
                "CONGELADO",
                "estado",
                "Presupuesto"
        );
        missingValidation.setValid(true);
        
        // Action
        List<TransitionViolation> violations = validator.validate(
                List.of(validAssignment, invalidAssignment, missingValidation), 
                config
        );
        
        // Expect
        assertEquals(2, violations.size(), 
                "Debe detectar 2 violaciones (1 inválida + 1 falta validación)");
        
        // Verificar que la transición válida no generó violación
        long invalidCount = violations.stream()
                .filter(v -> v.getViolationType() == TransitionViolation.ViolationType.INVALID_TRANSITION)
                .count();
        long missingValidationCount = violations.stream()
                .filter(v -> v.getViolationType() == TransitionViolation.ViolationType.MISSING_VALIDATION)
                .count();
        
        assertEquals(1, invalidCount, "Debe tener 1 violación de transición inválida");
        assertEquals(1, missingValidationCount, "Debe tener 1 advertencia de validación faltante");
    }
    
    @Test
    void deberiaManejarListaVacia() {
        // Action
        List<TransitionViolation> violations = validator.validate(List.of(), config);
        
        // Expect
        assertTrue(violations.isEmpty(), "Lista vacía no debe generar violaciones");
    }
    
    @Test
    void deberiaManejarConfigNull() {
        StateAssignment assignment = new StateAssignment(
                "Presupuesto.java",
                10,
                "aprobar",
                "BORRADOR",
                "CONGELADO",
                "estado",
                "Presupuesto"
        );
        assignment.setValid(true);
        
        // Action
        List<TransitionViolation> violations = validator.validate(List.of(assignment), null);
        
        // Expect
        assertTrue(violations.isEmpty(), "Config null debe retornar lista vacía");
    }
    
    @Test
    void deberiaIncluirTransicionesValidasEnMensaje() {
        // Setup: Transición inválida desde BORRADOR
        StateAssignment assignment = new StateAssignment(
                "Presupuesto.java",
                15,
                "invalidar",
                "BORRADOR",
                "INVALIDADO",  // No permitido desde BORRADOR
                "estado",
                "Presupuesto"
        );
        assignment.setValid(true);
        
        // Action
        List<TransitionViolation> violations = validator.validate(List.of(assignment), config);
        
        // Expect
        assertEquals(1, violations.size());
        TransitionViolation violation = violations.get(0);
        assertNotNull(violation.getValidTransitions());
        assertTrue(violation.getMessage().contains("Transiciones válidas"));
        assertTrue(violation.getMessage().contains("CONGELADO")); // La única transición válida
    }
}
