package com.budgetpro.validator.config;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para StateMachineConfigLoader.
 */
class StateMachineConfigLoaderTest {

    @Test
    void deberiaCargarConfiguracionValidaDesdeRecursos() throws StateMachineConfigLoader.ConfigurationException {
        StateMachineConfigLoader loader = new StateMachineConfigLoader();
        StateMachineConfig config = loader.loadConfig();

        assertNotNull(config);
        assertNotNull(config.getStateMachines());
        assertFalse(config.getStateMachines().isEmpty());

        // Verificar que se cargó la configuración de Presupuesto
        StateMachineConfig.StateMachineDefinition presupuestoDef = config.getStateMachines().stream()
            .filter(def -> def.getClassFqn().contains("Presupuesto"))
            .findFirst()
            .orElse(null);

        assertNotNull(presupuestoDef, "Debe existir la configuración de Presupuesto");
        assertEquals("com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto", presupuestoDef.getClassFqn());
        assertEquals("estado", presupuestoDef.getStateField());
        assertEquals("EstadoPresupuesto", presupuestoDef.getStateEnum());
    }

    @Test
    void deberiaCargarTransicionesCorrectamente() throws StateMachineConfigLoader.ConfigurationException {
        StateMachineConfigLoader loader = new StateMachineConfigLoader();
        StateMachineConfig config = loader.loadConfig();

        StateMachineConfig.StateMachineDefinition presupuestoDef = config.getStateMachines().stream()
            .filter(def -> def.getClassFqn().contains("Presupuesto"))
            .findFirst()
            .orElse(null);

        assertNotNull(presupuestoDef);
        Map<String, List<String>> transitions = presupuestoDef.getTransitions();
        
        assertNotNull(transitions);
        assertTrue(transitions.containsKey("BORRADOR"));
        assertEquals(List.of("CONGELADO"), transitions.get("BORRADOR"));
        assertTrue(transitions.containsKey("CONGELADO"));
        assertEquals(List.of(), transitions.get("CONGELADO"));
        assertTrue(transitions.containsKey("INVALIDADO"));
        assertEquals(List.of(), transitions.get("INVALIDADO"));
    }

    @Test
    void deberiaCargarMetodosDeTransicionCorrectamente() throws StateMachineConfigLoader.ConfigurationException {
        StateMachineConfigLoader loader = new StateMachineConfigLoader();
        StateMachineConfig config = loader.loadConfig();

        StateMachineConfig.StateMachineDefinition presupuestoDef = config.getStateMachines().stream()
            .filter(def -> def.getClassFqn().contains("Presupuesto"))
            .findFirst()
            .orElse(null);

        assertNotNull(presupuestoDef);
        List<String> transitionMethods = presupuestoDef.getTransitionMethods();
        
        assertNotNull(transitionMethods);
        assertTrue(transitionMethods.contains("aprobar"));
        assertTrue(transitionMethods.contains("congelar"));
        assertTrue(transitionMethods.contains("invalidar"));
    }

    @Test
    void deberiaCargarConfiguracionDesdeInputStream() throws StateMachineConfigLoader.ConfigurationException {
        String yamlContent = """
            stateMachines:
              - class: com.example.TestClass
                stateField: estado
                stateEnum: EstadoTest
                transitions:
                  INICIAL:
                    - FINAL
                  FINAL: []
                transitionMethods:
                  - avanzar
            """;

        InputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
        StateMachineConfigLoader loader = new StateMachineConfigLoader();
        StateMachineConfig config = loader.loadConfig(inputStream);

        assertNotNull(config);
        assertEquals(1, config.getStateMachines().size());
        
        StateMachineConfig.StateMachineDefinition def = config.getStateMachines().get(0);
        assertEquals("com.example.TestClass", def.getClassFqn());
        assertEquals("estado", def.getStateField());
        assertEquals("EstadoTest", def.getStateEnum());
    }

    @Test
    void deberiaRechazarYAMLConCampoFaltante() {
        String yamlContent = """
            stateMachines:
              - stateField: estado
                stateEnum: EstadoTest
                transitions:
                  INICIAL: []
                transitionMethods:
                  - avanzar
            """;

        InputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
        StateMachineConfigLoader loader = new StateMachineConfigLoader();

        StateMachineConfigLoader.ConfigurationException exception = assertThrows(
            StateMachineConfigLoader.ConfigurationException.class,
            () -> loader.loadConfig(inputStream)
        );

        assertTrue(exception.getMessage().contains("validation failed"));
        assertTrue(exception.getMessage().contains("'class' field is required"));
    }

    @Test
    void deberiaRechazarYAMLConTransicionesMalformadas() {
        String yamlContent = """
            stateMachines:
              - class: com.example.TestClass
                stateField: estado
                stateEnum: EstadoTest
                transitions:
                  INICIAL: null
                transitionMethods:
                  - avanzar
            """;

        InputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
        StateMachineConfigLoader loader = new StateMachineConfigLoader();

        StateMachineConfigLoader.ConfigurationException exception = assertThrows(
            StateMachineConfigLoader.ConfigurationException.class,
            () -> loader.loadConfig(inputStream)
        );

        assertTrue(exception.getMessage().contains("validation failed"));
        assertTrue(exception.getMessage().contains("transitions"));
    }

    @Test
    void deberiaRechazarYAMLConListaVaciaDeStateMachines() {
        String yamlContent = """
            stateMachines: []
            """;

        InputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
        StateMachineConfigLoader loader = new StateMachineConfigLoader();

        StateMachineConfigLoader.ConfigurationException exception = assertThrows(
            StateMachineConfigLoader.ConfigurationException.class,
            () -> loader.loadConfig(inputStream)
        );

        assertTrue(exception.getMessage().contains("validation failed"));
        assertTrue(exception.getMessage().contains("at least one 'stateMachines' entry"));
    }

    @Test
    void deberiaRechazarYAMLConMetodosDeTransicionNulos() {
        String yamlContent = """
            stateMachines:
              - class: com.example.TestClass
                stateField: estado
                stateEnum: EstadoTest
                transitions:
                  INICIAL: []
                transitionMethods:
                  - null
                  - ""
            """;

        InputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
        StateMachineConfigLoader loader = new StateMachineConfigLoader();

        StateMachineConfigLoader.ConfigurationException exception = assertThrows(
            StateMachineConfigLoader.ConfigurationException.class,
            () -> loader.loadConfig(inputStream)
        );

        assertTrue(exception.getMessage().contains("validation failed"));
        assertTrue(exception.getMessage().contains("transitionMethods"));
    }

    @Test
    void deberiaRechazarInputStreamNulo() {
        StateMachineConfigLoader loader = new StateMachineConfigLoader();

        assertThrows(
            NullPointerException.class,
            () -> loader.loadConfig((InputStream) null)
        );
    }
}
