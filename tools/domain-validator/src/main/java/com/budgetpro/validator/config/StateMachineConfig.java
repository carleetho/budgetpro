package com.budgetpro.validator.config;

import java.util.List;
import java.util.Map;

/**
 * Representa la configuración de máquinas de estado desde un archivo YAML.
 * 
 * Define las reglas de validación para transiciones de estado en entidades del dominio.
 */
public class StateMachineConfig {
    
    private List<StateMachineDefinition> stateMachines;

    /**
     * Constructor por defecto para deserialización YAML.
     */
    public StateMachineConfig() {
    }

    /**
     * Constructor con lista de definiciones.
     */
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
     * Valida que la configuración sea consistente.
     * 
     * @return Lista de errores encontrados, o lista vacía si es válida.
     */
    public List<String> validate() {
        if (stateMachines == null || stateMachines.isEmpty()) {
            return List.of("State machine configuration must define at least one 'stateMachines' entry");
        }

        List<String> errors = new java.util.ArrayList<>();
        for (int i = 0; i < stateMachines.size(); i++) {
            StateMachineDefinition def = stateMachines.get(i);
            List<String> defErrors = def.validate();
            for (String error : defErrors) {
                errors.add(String.format("State machine [%d]: %s", i, error));
            }
        }

        return errors;
    }

    /**
     * Representa una definición individual de máquina de estado.
     */
    public static class StateMachineDefinition {
        private String classFqn;
        private String stateField;
        private String stateEnum;
        private Map<String, List<String>> transitions;
        private List<String> transitionMethods;

        /**
         * Constructor por defecto para deserialización YAML.
         */
        public StateMachineDefinition() {
        }

        /**
         * Constructor completo.
         */
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

        /**
         * Valida que la definición de máquina de estado sea completa y correcta.
         * 
         * @return Lista de errores encontrados, o lista vacía si es válida.
         */
        public List<String> validate() {
            List<String> errors = new java.util.ArrayList<>();

            if (classFqn == null || classFqn.isBlank()) {
                errors.add("'class' field is required and cannot be empty");
            }

            if (stateField == null || stateField.isBlank()) {
                errors.add("'stateField' field is required and cannot be empty");
            }

            if (stateEnum == null || stateEnum.isBlank()) {
                errors.add("'stateEnum' field is required and cannot be empty");
            }

            if (transitions == null) {
                errors.add("'transitions' field is required");
            } else {
                // Validar que no haya valores nulos en el mapa
                for (Map.Entry<String, List<String>> entry : transitions.entrySet()) {
                    if (entry.getKey() == null || entry.getKey().isBlank()) {
                        errors.add("'transitions' map cannot contain null or empty keys");
                    }
                    if (entry.getValue() == null) {
                        errors.add(String.format("'transitions' map value for key '%s' cannot be null (use empty list [] instead)", entry.getKey()));
                    } else {
                        // Validar que no haya valores nulos en la lista
                        for (String targetState : entry.getValue()) {
                            if (targetState == null || targetState.isBlank()) {
                                errors.add(String.format("'transitions' map value for key '%s' cannot contain null or empty target states", entry.getKey()));
                            }
                        }
                    }
                }
            }

            if (transitionMethods == null) {
                errors.add("'transitionMethods' field is required");
            } else {
                // Validar que no haya valores nulos en la lista
                for (String method : transitionMethods) {
                    if (method == null || method.isBlank()) {
                        errors.add("'transitionMethods' list cannot contain null or empty method names");
                    }
                }
            }

            return errors;
        }
    }
}
