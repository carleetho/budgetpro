package com.budgetpro.validator.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Representa la configuración de máquinas de estado.
 * 
 * Define las reglas de validación para transiciones de estado en entidades del
 * dominio.
 */
public class StateMachineConfig {

    private List<StateMachineDefinition> stateMachines;

    public StateMachineConfig() {
    }

    public StateMachineConfig(List<StateMachineDefinition> stateMachines) {
        this.stateMachines = stateMachines;
    }

    public List<StateMachineDefinition> getStateMachines() {
        return stateMachines;
    }

    public void setStateMachines(List<StateMachineDefinition> stateMachines) {
        this.stateMachines = stateMachines;
    }

    /**
     * Busca la definición de máquina de estado para una clase.
     */
    public StateMachineDefinition findDefinitionForClass(String className) {
        if (stateMachines == null || className == null)
            return null;
        return stateMachines.stream()
                .filter(def -> def.getClassFqn().equals(className) || def.getClassFqn().endsWith("." + className))
                .findFirst().orElse(null);
    }

    public List<String> validate() {
        if (stateMachines == null || stateMachines.isEmpty()) {
            return List.of("State machine configuration must define at least one 'state_machines' entry");
        }

        List<String> errors = new ArrayList<>();
        for (int i = 0; i < stateMachines.size(); i++) {
            StateMachineDefinition def = stateMachines.get(i);
            List<String> defErrors = def.validate();
            for (String error : defErrors) {
                errors.add(String.format("State machine [%d]: %s", i, error));
            }
        }

        return errors;
    }

    public static class StateMachineDefinition {
        private String classFqn;
        private String stateField;
        private String stateEnum;
        private String initialState;
        private Set<String> terminalStates;
        private Map<String, List<String>> transitions;
        private List<String> transitionMethods;

        public StateMachineDefinition() {
        }

        public StateMachineDefinition(String classFqn, String stateField, String stateEnum,
                Map<String, List<String>> transitions, List<String> transitionMethods) {
            this.classFqn = classFqn;
            this.stateField = stateField;
            this.stateEnum = stateEnum;
            this.transitions = transitions;
            this.transitionMethods = transitionMethods;
        }

        public String getClassFqn() {
            return classFqn;
        }

        public void setClassFqn(String classFqn) {
            this.classFqn = classFqn;
        }

        public String getStateField() {
            return stateField;
        }

        public void setStateField(String stateField) {
            this.stateField = stateField;
        }

        public String getStateEnum() {
            return stateEnum;
        }

        public void setStateEnum(String stateEnum) {
            this.stateEnum = stateEnum;
        }

        public String getInitialState() {
            return initialState;
        }

        public void setInitialState(String initialState) {
            this.initialState = initialState;
        }

        public Set<String> getTerminalStates() {
            return terminalStates;
        }

        public void setTerminalStates(Set<String> terminalStates) {
            this.terminalStates = terminalStates;
        }

        public Map<String, List<String>> getTransitions() {
            return transitions;
        }

        public void setTransitions(Map<String, List<String>> transitions) {
            this.transitions = transitions;
        }

        public List<String> getTransitionMethods() {
            return transitionMethods;
        }

        public void setTransitionMethods(List<String> transitionMethods) {
            this.transitionMethods = transitionMethods;
        }

        public boolean isValidTransition(String fromState, String toState) {
            if (fromState == null || toState == null || transitions == null)
                return false;

            // Si es un estado terminal, no permite transiciones salientes
            if (isTerminalState(fromState))
                return false;

            List<String> allowed = transitions.get(fromState);
            return allowed != null && allowed.contains(toState);
        }

        public boolean isTerminalState(String state) {
            if (state == null)
                return false;
            if (terminalStates != null && terminalStates.contains(state))
                return true;

            // Heurística por defecto si no hay estados terminales definidos
            return "CONGELADO".equals(state) || "CERRADO".equals(state) || "CANCELADO".equals(state);
        }

        public List<String> validate() {
            List<String> errors = new ArrayList<>();

            if (classFqn == null || classFqn.isBlank())
                errors.add("'class_fqn' is required");
            if (stateField == null || stateField.isBlank())
                errors.add("'state_field' is required");
            if (stateEnum == null || stateEnum.isBlank())
                errors.add("'state_enum' is required");

            if (transitions == null) {
                errors.add("'transitions' is required");
            } else {
                for (Map.Entry<String, List<String>> entry : transitions.entrySet()) {
                    if (entry.getValue() == null) {
                        errors.add(String.format("Transitions for '%s' cannot be null", entry.getKey()));
                    }
                }
            }

            return errors;
        }
    }

    public static StateMachineConfig defaultConfig() {
        return new StateMachineConfig(new ArrayList<>());
    }
}
