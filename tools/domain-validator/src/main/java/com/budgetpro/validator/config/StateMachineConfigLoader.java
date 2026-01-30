package com.budgetpro.validator.config;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Cargador de la configuración de máquinas de estado desde archivo YAML.
 * 
 * Sigue el patrón establecido por RoadmapLoader y BoundaryConfigLoader.
 */
public class StateMachineConfigLoader {

    private static final String STATE_MACHINE_RULES_RESOURCE_PATH = "/state-machine-rules.yml";
    private final Yaml yaml;

    public StateMachineConfigLoader() {
        LoaderOptions options = new LoaderOptions();
        this.yaml = new Yaml(options);
    }

    /**
     * Carga la configuración de máquinas de estado desde el archivo de recursos predeterminado.
     * 
     * @return La configuración de máquinas de estado cargada
     * @throws ConfigurationException si hay error al cargar o validar la configuración
     */
    public StateMachineConfig loadConfig() throws ConfigurationException {
        try (InputStream inputStream = getClass().getResourceAsStream(STATE_MACHINE_RULES_RESOURCE_PATH)) {
            if (inputStream == null) {
                throw new ConfigurationException("State machine rules resource not found: " + STATE_MACHINE_RULES_RESOURCE_PATH);
            }
            return loadConfig(inputStream);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Failed to load state machine configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Carga la configuración de máquinas de estado desde un InputStream personalizado.
     * Útil para testing.
     * 
     * @param inputStream El stream de entrada con el contenido YAML
     * @return La configuración de máquinas de estado cargada
     * @throws ConfigurationException si hay error al cargar o validar la configuración
     */
    public StateMachineConfig loadConfig(InputStream inputStream) throws ConfigurationException {
        Objects.requireNonNull(inputStream, "inputStream cannot be null");

        try {
            // SnakeYAML carga el YAML como Map/List, necesitamos convertirlo manualmente
            Object loaded = yaml.load(inputStream);

            if (loaded == null) {
                throw new ConfigurationException("State machine configuration is empty or malformed");
            }

            if (!(loaded instanceof Map)) {
                throw new ConfigurationException("State machine configuration must be a YAML map");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> root = (Map<String, Object>) loaded;

            StateMachineConfig config = mapToConfig(root);

            // Validar estructura
            List<String> validationErrors = config.validate();
            if (!validationErrors.isEmpty()) {
                throw new ConfigurationException("State machine configuration validation failed:\n" + 
                    String.join("\n", validationErrors));
            }

            return config;

        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Failed to parse state machine configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Convierte el Map cargado desde YAML a StateMachineConfig.
     */
    @SuppressWarnings("unchecked")
    private StateMachineConfig mapToConfig(Map<String, Object> root) throws ConfigurationException {
        Object stateMachinesObj = root.get("stateMachines");
        if (stateMachinesObj == null) {
            throw new ConfigurationException("Missing required field: 'stateMachines'");
        }

        if (!(stateMachinesObj instanceof List)) {
            throw new ConfigurationException("'stateMachines' must be a list");
        }

        List<Object> stateMachinesList = (List<Object>) stateMachinesObj;
        List<StateMachineConfig.StateMachineDefinition> definitions = stateMachinesList.stream()
            .map(obj -> {
                if (!(obj instanceof Map)) {
                    throw new RuntimeException("Each state machine definition must be a map");
                }
                return mapToDefinition((Map<String, Object>) obj);
            })
            .collect(Collectors.toList());

        return new StateMachineConfig(definitions);
    }

    /**
     * Convierte un Map individual a StateMachineDefinition.
     */
    @SuppressWarnings("unchecked")
    private StateMachineConfig.StateMachineDefinition mapToDefinition(Map<String, Object> map) {
        StateMachineConfig.StateMachineDefinition def = new StateMachineConfig.StateMachineDefinition();

        // class
        Object classObj = map.get("class");
        if (classObj != null) {
            def.setClassFqn(classObj.toString());
        }

        // stateField
        Object stateFieldObj = map.get("stateField");
        if (stateFieldObj != null) {
            def.setStateField(stateFieldObj.toString());
        }

        // stateEnum
        Object stateEnumObj = map.get("stateEnum");
        if (stateEnumObj != null) {
            def.setStateEnum(stateEnumObj.toString());
        }

        // transitions
        Object transitionsObj = map.get("transitions");
        if (transitionsObj instanceof Map) {
            Map<String, Object> transitionsMap = (Map<String, Object>) transitionsObj;
            Map<String, List<String>> transitions = new java.util.HashMap<>();
            for (Map.Entry<String, Object> entry : transitionsMap.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    // null explícito en YAML - esto es un error, pero lo manejamos en validación
                    transitions.put(entry.getKey(), null); // Dejamos null para que la validación lo detecte
                } else if (value instanceof List) {
                    List<String> targetStates = ((List<?>) value).stream()
                        .map(v -> v != null ? v.toString() : "")
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                    transitions.put(entry.getKey(), targetStates);
                } else {
                    transitions.put(entry.getKey(), List.of());
                }
            }
            def.setTransitions(transitions);
        }

        // transitionMethods
        Object transitionMethodsObj = map.get("transitionMethods");
        if (transitionMethodsObj instanceof List) {
            List<String> transitionMethods = ((List<?>) transitionMethodsObj).stream()
                .map(obj -> obj != null ? obj.toString() : "")
                .collect(Collectors.toList());
            def.setTransitionMethods(transitionMethods);
        }

        return def;
    }

    /**
     * Excepción lanzada cuando hay error al cargar la configuración.
     */
    public static class ConfigurationException extends Exception {
        public ConfigurationException(String message) {
            super(message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
