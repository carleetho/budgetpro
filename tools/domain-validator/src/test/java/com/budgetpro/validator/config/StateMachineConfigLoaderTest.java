package com.budgetpro.validator.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
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
                StateMachineConfig config = loader.loadDefault();

                assertNotNull(config);
                assertNotNull(config.getStateMachines());
                assertFalse(config.getStateMachines().isEmpty());

                // Verificar que se cargó la configuración de Presupuesto
                StateMachineConfig.StateMachineDefinition presupuestoDef = config.getStateMachines().stream()
                                .filter(def -> def.getClassFqn().contains("Presupuesto")).findFirst().orElse(null);

                assertNotNull(presupuestoDef, "Debe existir la configuración de Presupuesto");
                assertEquals("com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto",
                                presupuestoDef.getClassFqn());
                assertEquals("estado", presupuestoDef.getStateField());
                assertEquals("EstadoPresupuesto", presupuestoDef.getStateEnum());
        }

        @Test
        void deberiaCargarTransicionesCorrectamente() throws StateMachineConfigLoader.ConfigurationException {
                StateMachineConfigLoader loader = new StateMachineConfigLoader();
                StateMachineConfig config = loader.loadDefault();

                StateMachineConfig.StateMachineDefinition presupuestoDef = config.getStateMachines().stream()
                                .filter(def -> def.getClassFqn().contains("Presupuesto")).findFirst().orElse(null);

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
                StateMachineConfig config = loader.loadDefault();

                StateMachineConfig.StateMachineDefinition presupuestoDef = config.getStateMachines().stream()
                                .filter(def -> def.getClassFqn().contains("Presupuesto")).findFirst().orElse(null);

                assertNotNull(presupuestoDef);
                List<String> transitionMethods = presupuestoDef.getTransitionMethods();

                assertNotNull(transitionMethods);
                assertTrue(transitionMethods.contains("aprobar"));
                assertTrue(transitionMethods.contains("congelar"));
                assertTrue(transitionMethods.contains("invalidar"));
        }

        @Test
        void deberiaCargarConfiguracionDesdeArchivo(@TempDir Path tempDir) throws Exception {
                String yamlContent = """
                                state_machines:
                                  - class_fqn: com.example.TestClass
                                    state_field: estado
                                    state_enum: EstadoTest
                                    transitions:
                                      INICIAL:
                                        - FINAL
                                      FINAL: []
                                    transition_methods:
                                      - avanzar
                                """;

                Path configFile = tempDir.resolve("test-config.yml");
                Files.writeString(configFile, yamlContent);

                StateMachineConfigLoader loader = new StateMachineConfigLoader();
                StateMachineConfig config = loader.loadFromFile(configFile);

                assertNotNull(config);
                assertEquals(1, config.getStateMachines().size());

                StateMachineConfig.StateMachineDefinition def = config.getStateMachines().get(0);
                assertEquals("com.example.TestClass", def.getClassFqn());
                assertEquals("estado", def.getStateField());
                assertEquals("EstadoTest", def.getStateEnum());
        }

        @Test
        void deberiaRechazarYAMLConCampoFaltante(@TempDir Path tempDir) throws Exception {
                String yamlContent = """
                                state_machines:
                                  - state_field: estado
                                    state_enum: EstadoTest
                                    transitions:
                                      INICIAL: []
                                    transition_methods:
                                      - avanzar
                                """;

                Path configFile = tempDir.resolve("invalid-config.yml");
                Files.writeString(configFile, yamlContent);

                StateMachineConfigLoader loader = new StateMachineConfigLoader();

                StateMachineConfigLoader.ConfigurationException exception = assertThrows(
                                StateMachineConfigLoader.ConfigurationException.class,
                                () -> loader.loadFromFile(configFile));

                assertTrue(exception.getMessage().contains("validation failed"));
                assertTrue(exception.getMessage().contains("'class' field is required"));
        }

        @Test
        void deberiaRechazarYAMLConTransicionesMalformadas(@TempDir Path tempDir) throws Exception {
                String yamlContent = """
                                state_machines:
                                  - class_fqn: com.example.TestClass
                                    state_field: estado
                                    state_enum: EstadoTest
                                    transitions:
                                      INICIAL: null
                                    transition_methods:
                                      - avanzar
                                """;

                Path configFile = tempDir.resolve("malformed-config.yml");
                Files.writeString(configFile, yamlContent);

                StateMachineConfigLoader loader = new StateMachineConfigLoader();

                StateMachineConfigLoader.ConfigurationException exception = assertThrows(
                                StateMachineConfigLoader.ConfigurationException.class,
                                () -> loader.loadFromFile(configFile));

                assertTrue(exception.getMessage().contains("validation failed"));
                assertTrue(exception.getMessage().contains("transitions"));
        }

        @Test
        void deberiaRechazarYAMLConListaVaciaDeStateMachines(@TempDir Path tempDir) throws Exception {
                String yamlContent = """
                                state_machines: []
                                """;

                Path configFile = tempDir.resolve("empty-config.yml");
                Files.writeString(configFile, yamlContent);

                StateMachineConfigLoader loader = new StateMachineConfigLoader();

                StateMachineConfigLoader.ConfigurationException exception = assertThrows(
                                StateMachineConfigLoader.ConfigurationException.class,
                                () -> loader.loadFromFile(configFile));

                assertTrue(exception.getMessage().contains("validation failed"));
                assertTrue(exception.getMessage().contains("at least one 'stateMachines' entry"));
        }

        @Test
        void deberiaRechazarYAMLConMetodosDeTransicionNulos(@TempDir Path tempDir) throws Exception {
                String yamlContent = """
                                state_machines:
                                  - class_fqn: com.example.TestClass
                                    state_field: estado
                                    state_enum: EstadoTest
                                    transitions:
                                      INICIAL: []
                                    transition_methods:
                                      - null
                                      - ""
                                """;

                Path configFile = tempDir.resolve("null-methods-config.yml");
                Files.writeString(configFile, yamlContent);

                StateMachineConfigLoader loader = new StateMachineConfigLoader();

                StateMachineConfigLoader.ConfigurationException exception = assertThrows(
                                StateMachineConfigLoader.ConfigurationException.class,
                                () -> loader.loadFromFile(configFile));

                assertTrue(exception.getMessage().contains("validation failed"));
                assertTrue(exception.getMessage().contains("transitionMethods"));
        }

        @Test
        void deberiaRechazarArchivoInexistente(@TempDir Path tempDir) {
                StateMachineConfigLoader loader = new StateMachineConfigLoader();
                Path nonExistentFile = tempDir.resolve("non-existent.yml");

                assertThrows(StateMachineConfigLoader.ConfigurationException.class,
                                () -> loader.loadFromFile(nonExistentFile));
        }
}
