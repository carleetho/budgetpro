package com.budgetpro.validator.analyzer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Configuración de máquinas de estado para validación de asignaciones.
 * 
 * Encapsula el mapa de enums de estado y sus valores para validar
 * que las asignaciones de estado usen valores válidos.
 * También incluye reglas de transiciones válidas.
 */
public class StateMachineConfig {
    
    private final Map<String, List<String>> stateMachines;
    private final Map<String, Map<String, List<String>>> transitions; // className -> (fromState -> [toStates])
    private final Map<String, Set<String>> finalStates; // className -> [finalStates]
    
    public StateMachineConfig(Map<String, List<String>> stateMachines) {
        this.stateMachines = stateMachines;
        this.transitions = new HashMap<>();
        this.finalStates = new HashMap<>();
    }
    
    /**
     * Constructor completo con transiciones y estados finales.
     */
    public StateMachineConfig(Map<String, List<String>> stateMachines,
                             Map<String, Map<String, List<String>>> transitions,
                             Map<String, Set<String>> finalStates) {
        this.stateMachines = stateMachines;
        this.transitions = transitions != null ? transitions : new HashMap<>();
        this.finalStates = finalStates != null ? finalStates : new HashMap<>();
    }
    
    /**
     * Verifica si un valor de estado existe en el enum especificado.
     * 
     * @param enumName Nombre del enum (puede ser FQN o nombre simple)
     * @param stateValue Valor del estado a verificar
     * @return true si el estado existe en el enum
     */
    public boolean isValidState(String enumName, String stateValue) {
        if (enumName == null || stateValue == null) {
            return false;
        }
        
        List<String> states = stateMachines.get(enumName);
        if (states == null) {
            return false;
        }
        
        return states.contains(stateValue);
    }
    
    /**
     * Obtiene todos los valores válidos para un enum de estado.
     * 
     * @param enumName Nombre del enum
     * @return Lista de valores válidos, o lista vacía si el enum no existe
     */
    public List<String> getValidStates(String enumName) {
        if (enumName == null) {
            return List.of();
        }
        
        return stateMachines.getOrDefault(enumName, List.of());
    }
    
    /**
     * Obtiene el mapa completo de máquinas de estado.
     */
    public Map<String, List<String>> getStateMachines() {
        return stateMachines;
    }
    
    /**
     * Obtiene las transiciones válidas para una clase desde un estado específico.
     * 
     * @param className Nombre de la clase (ej: "Presupuesto", "Proyecto")
     * @param fromState Estado origen
     * @return Lista de estados destino permitidos, o lista vacía si no hay transiciones permitidas
     */
    public List<String> getAllowedTransitions(String className, String fromState) {
        if (className == null || fromState == null) {
            return List.of();
        }
        
        Map<String, List<String>> classTransitions = transitions.get(className);
        if (classTransitions == null) {
            return List.of();
        }
        
        return classTransitions.getOrDefault(fromState, List.of());
    }
    
    /**
     * Verifica si un estado es final (no permite transiciones).
     * 
     * @param className Nombre de la clase
     * @param state Estado a verificar
     * @return true si el estado es final
     */
    public boolean isFinalState(String className, String state) {
        if (className == null || state == null) {
            return false;
        }
        
        Set<String> finals = finalStates.get(className);
        if (finals == null) {
            // Estados finales conocidos por defecto
            return "CONGELADO".equals(state) || "CERRADO".equals(state);
        }
        
        return finals.contains(state);
    }
    
    /**
     * Verifica si una transición es válida.
     * 
     * @param className Nombre de la clase
     * @param fromState Estado origen
     * @param toState Estado destino
     * @return true si la transición está permitida
     */
    public boolean isValidTransition(String className, String fromState, String toState) {
        if (className == null || fromState == null || toState == null) {
            return false;
        }
        
        // Estados finales no permiten transiciones
        if (isFinalState(className, fromState)) {
            return false;
        }
        
        List<String> allowed = getAllowedTransitions(className, fromState);
        return allowed.contains(toState);
    }
    
    /**
     * Obtiene el mapa de transiciones.
     */
    public Map<String, Map<String, List<String>>> getTransitions() {
        return transitions;
    }
    
    /**
     * Obtiene el mapa de estados finales.
     */
    public Map<String, Set<String>> getFinalStates() {
        return finalStates;
    }
}
