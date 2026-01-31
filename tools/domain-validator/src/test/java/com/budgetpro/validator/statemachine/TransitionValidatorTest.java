package com.budgetpro.validator.statemachine;

import com.budgetpro.validator.config.StateMachineConfig;
import com.budgetpro.validator.model.StateAssignment;
import com.budgetpro.validator.model.TransitionViolation;
import com.budgetpro.validator.model.ViolationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

                // Crear definición para Presupuesto
                StateMachineConfig.StateMachineDefinition def = new StateMachineConfig.StateMachineDefinition();
                def.setClassFqn("com.budgetpro.domain.Presupuesto");
                def.setStateField("estado");
                def.setStateEnum("EstadoPresupuesto");
                def.setInitialState("BORRADOR");
                def.setTerminalStates(java.util.Set.of("INVALIDADO", "CONGELADO"));

                Map<String, List<String>> transitions = new HashMap<>();
                transitions.put("BORRADOR", List.of("CONGELADO", "INVALIDADO"));
                def.setTransitions(transitions);

                config = new StateMachineConfig(List.of(def));
        }

        @Test
        void deberiaPasarTransicionValida() {
                // Setup: Transición válida BORRADOR → CONGELADO
                StateAssignment assignment = new StateAssignment("Presupuesto.java", 10, "aprobar", "BORRADOR",
                                "CONGELADO", "estado", "Presupuesto");

                // Action
                List<TransitionViolation> violations = validator.validate(List.of(assignment), config);

                // Expect
                assertTrue(violations.isEmpty(), "Transición válida no debe generar violaciones");
        }

        @Test
        void deberiaDetectarTransicionInvalida() {
                // Setup: Transición inválida CONGELADO → INVALIDADO (CONGELADO es terminal)
                StateAssignment assignment = new StateAssignment("Presupuesto.java", 15, "invalidar", "CONGELADO",
                                "INVALIDADO", "estado", "Presupuesto");

                // Action
                List<TransitionViolation> violations = validator.validate(List.of(assignment), config);

                // Expect
                assertEquals(1, violations.size(), "Debe detectar una violación");

                TransitionViolation violation = violations.get(0);
                assertEquals(ViolationSeverity.CRITICAL, violation.getSeverity());
                assertEquals(TransitionViolation.ViolationType.INVALID_TRANSITION, violation.getViolationType());
                assertEquals("CONGELADO", violation.getFromState());
                assertTrue(violation.getMessage().contains("terminal"));
        }

        @Test
        void deberiaGenerarAdvertenciaPorValidacionFaltante() {
                // Setup: Asignación sin estado origen (fromState = null)
                StateAssignment assignment = new StateAssignment("Presupuesto.java", 25, "cambiarEstado", null,
                                "CONGELADO", "estado", "Presupuesto");

                // Action
                List<TransitionViolation> violations = validator.validate(List.of(assignment), config);

                // Expect
                assertEquals(1, violations.size(), "Debe detectar falta de validación");

                TransitionViolation violation = violations.get(0);
                assertEquals(ViolationSeverity.WARNING, violation.getSeverity());
                assertEquals(TransitionViolation.ViolationType.MISSING_VALIDATION, violation.getViolationType());
                assertNull(violation.getFromState());
                assertTrue(violation.getMessage().contains("Falta validación de estado"));
        }

        @Test
        void deberiaManejarConfigNull() {
                StateAssignment assignment = new StateAssignment("Presupuesto.java", 10, "aprobar", "BORRADOR",
                                "CONGELADO", "estado", "Presupuesto");

                // Action
                List<TransitionViolation> violations = validator.validate(List.of(assignment), null);

                // Expect
                assertTrue(violations.isEmpty(), "Config null debe retornar lista vacía");
        }
}
