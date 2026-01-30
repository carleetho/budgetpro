package com.budgetpro.blastradius.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para ConfigLoader.
 */
class ConfigLoaderTest {
    
    private final ConfigLoader configLoader = new ConfigLoader();
    
    @Test
    void testLoadConfigurationFromJsonFile(@TempDir Path tempDir) throws Exception {
        // Setup: Crear archivo JSON de prueba con valores personalizados
        String jsonContent = """
            {
              "max_files_without_approval": 15,
              "max_files_red_zone": 2,
              "max_files_yellow_zone": 5,
              "red_zone_paths": [
                "domain/test/",
                "domain/custom/"
              ],
              "yellow_zone_paths": [
                "infrastructure/custom/"
              ],
              "override_keyword": "CUSTOM_APPROVED"
            }
            """;
        
        Path configFile = tempDir.resolve("test-config.json");
        Files.writeString(configFile, jsonContent);
        
        // Action: Cargar configuración
        BlastRadiusConfig config = configLoader.load(configFile);
        
        // Expect: Configuración con valores correctos
        assertEquals(15, config.getMaxFilesWithoutApproval());
        assertEquals(2, config.getMaxFilesRedZone());
        assertEquals(5, config.getMaxFilesYellowZone());
        assertEquals(List.of("domain/test/", "domain/custom/"), config.getRedZonePaths());
        assertEquals(List.of("infrastructure/custom/"), config.getYellowZonePaths());
        assertEquals("CUSTOM_APPROVED", config.getOverrideKeyword());
    }
    
    @Test
    void testUseDefaultConfigurationWhenFileMissing() throws Exception {
        // Setup: No hay archivo de configuración
        
        // Action: Cargar configuración por defecto
        BlastRadiusConfig config = configLoader.loadDefaults();
        
        // Expect: Configuración con valores por defecto de los requisitos
        assertEquals(10, config.getMaxFilesWithoutApproval());
        assertEquals(1, config.getMaxFilesRedZone());
        assertEquals(3, config.getMaxFilesYellowZone());
        assertTrue(config.getRedZonePaths().contains("domain/presupuesto/"));
        assertTrue(config.getRedZonePaths().contains("domain/estimacion/"));
        assertTrue(config.getRedZonePaths().contains("domain/valueobjects/"));
        assertTrue(config.getRedZonePaths().contains("domain/entities/"));
        assertTrue(config.getYellowZonePaths().contains("infrastructure/persistence/"));
        assertEquals("BIGBANG_APPROVED", config.getOverrideKeyword());
        
        // Verificar que la configuración es válida
        assertTrue(config.validate().isEmpty());
    }
    
    @Test
    void testValidationRejectsInvalidConfiguration(@TempDir Path tempDir) throws Exception {
        // Setup: JSON con max_files_without_approval negativo
        String invalidJson = """
            {
              "max_files_without_approval": -5,
              "max_files_red_zone": 1,
              "max_files_yellow_zone": 3,
              "red_zone_paths": [
                "domain/test/"
              ],
              "yellow_zone_paths": [
                "infrastructure/test/"
              ],
              "override_keyword": "TEST_APPROVED"
            }
            """;
        
        Path configFile = tempDir.resolve("invalid-config.json");
        Files.writeString(configFile, invalidJson);
        
        // Action: Intentar cargar configuración inválida
        ConfigLoader.ConfigLoadException exception = assertThrows(
            ConfigLoader.ConfigLoadException.class,
            () -> configLoader.load(configFile)
        );
        
        // Expect: Excepción con mensaje de error claro
        assertTrue(exception.getMessage().contains("validation failed"));
        assertTrue(exception.getMessage().contains("max_files_without_approval"));
    }
    
    @Test
    void testValidationRejectsEmptyRedZonePaths(@TempDir Path tempDir) throws Exception {
        // Setup: JSON con red_zone_paths vacío
        String invalidJson = """
            {
              "max_files_without_approval": 10,
              "max_files_red_zone": 1,
              "max_files_yellow_zone": 3,
              "red_zone_paths": [],
              "yellow_zone_paths": [
                "infrastructure/test/"
              ],
              "override_keyword": "TEST_APPROVED"
            }
            """;
        
        Path configFile = tempDir.resolve("invalid-config.json");
        Files.writeString(configFile, invalidJson);
        
        // Action: Intentar cargar configuración inválida
        ConfigLoader.ConfigLoadException exception = assertThrows(
            ConfigLoader.ConfigLoadException.class,
            () -> configLoader.load(configFile)
        );
        
        // Expect: Excepción indicando que red_zone_paths debe ser no vacío
        assertTrue(exception.getMessage().contains("validation failed"));
        assertTrue(exception.getMessage().contains("red_zone_paths"));
    }
    
    @Test
    void testValidationRejectsEmptyYellowZonePaths(@TempDir Path tempDir) throws Exception {
        // Setup: JSON con yellow_zone_paths vacío
        String invalidJson = """
            {
              "max_files_without_approval": 10,
              "max_files_red_zone": 1,
              "max_files_yellow_zone": 3,
              "red_zone_paths": [
                "domain/test/"
              ],
              "yellow_zone_paths": [],
              "override_keyword": "TEST_APPROVED"
            }
            """;
        
        Path configFile = tempDir.resolve("invalid-config.json");
        Files.writeString(configFile, invalidJson);
        
        // Action: Intentar cargar configuración inválida
        ConfigLoader.ConfigLoadException exception = assertThrows(
            ConfigLoader.ConfigLoadException.class,
            () -> configLoader.load(configFile)
        );
        
        // Expect: Excepción indicando que yellow_zone_paths debe ser no vacío
        assertTrue(exception.getMessage().contains("validation failed"));
        assertTrue(exception.getMessage().contains("yellow_zone_paths"));
    }
    
    @Test
    void testValidationRejectsZeroMaxFiles(@TempDir Path tempDir) throws Exception {
        // Setup: JSON con max_files_red_zone = 0
        String invalidJson = """
            {
              "max_files_without_approval": 10,
              "max_files_red_zone": 0,
              "max_files_yellow_zone": 3,
              "red_zone_paths": [
                "domain/test/"
              ],
              "yellow_zone_paths": [
                "infrastructure/test/"
              ],
              "override_keyword": "TEST_APPROVED"
            }
            """;
        
        Path configFile = tempDir.resolve("invalid-config.json");
        Files.writeString(configFile, invalidJson);
        
        // Action: Intentar cargar configuración inválida
        ConfigLoader.ConfigLoadException exception = assertThrows(
            ConfigLoader.ConfigLoadException.class,
            () -> configLoader.load(configFile)
        );
        
        // Expect: Excepción indicando que max_files_red_zone debe ser positivo
        assertTrue(exception.getMessage().contains("validation failed"));
        assertTrue(exception.getMessage().contains("max_files_red_zone"));
    }
    
    @Test
    void testLoadNonExistentFileThrowsException(@TempDir Path tempDir) {
        // Setup: Archivo que no existe
        Path nonExistentFile = tempDir.resolve("non-existent.json");
        
        // Action: Intentar cargar archivo inexistente
        ConfigLoader.ConfigLoadException exception = assertThrows(
            ConfigLoader.ConfigLoadException.class,
            () -> configLoader.load(nonExistentFile)
        );
        
        // Expect: Excepción indicando que el archivo no se encontró
        assertTrue(exception.getMessage().contains("not found"));
    }
    
    @Test
    void testConfigWithMissingFieldsUsesDefaults(@TempDir Path tempDir) throws Exception {
        // Setup: JSON con algunos campos faltantes
        String partialJson = """
            {
              "red_zone_paths": [
                "domain/test/"
              ],
              "yellow_zone_paths": [
                "infrastructure/test/"
              ]
            }
            """;
        
        Path configFile = tempDir.resolve("partial-config.json");
        Files.writeString(configFile, partialJson);
        
        // Action: Cargar configuración parcial
        BlastRadiusConfig config = configLoader.load(configFile);
        
        // Expect: Campos faltantes usan valores por defecto
        assertEquals(10, config.getMaxFilesWithoutApproval()); // default
        assertEquals(1, config.getMaxFilesRedZone()); // default
        assertEquals(3, config.getMaxFilesYellowZone()); // default
        assertEquals("BIGBANG_APPROVED", config.getOverrideKeyword()); // default
        assertEquals(List.of("domain/test/"), config.getRedZonePaths());
        assertEquals(List.of("infrastructure/test/"), config.getYellowZonePaths());
    }
    
    @Test
    void testBlastRadiusConfigBuilder() {
        // Test del builder pattern para tests
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .maxFilesWithoutApproval(20)
            .maxFilesRedZone(2)
            .maxFilesYellowZone(4)
            .redZonePaths(Arrays.asList("path1", "path2"))
            .yellowZonePaths(Arrays.asList("path3"))
            .overrideKeyword("CUSTOM")
            .build();
        
        assertEquals(20, config.getMaxFilesWithoutApproval());
        assertEquals(2, config.getMaxFilesRedZone());
        assertEquals(4, config.getMaxFilesYellowZone());
        assertEquals(Arrays.asList("path1", "path2"), config.getRedZonePaths());
        assertEquals(Arrays.asList("path3"), config.getYellowZonePaths());
        assertEquals("CUSTOM", config.getOverrideKeyword());
    }
    
    @Test
    void testBlastRadiusConfigValidation() {
        // Test de validación directa
        BlastRadiusConfig invalidConfig = BlastRadiusConfig.builder()
            .maxFilesWithoutApproval(-1)
            .redZonePaths(Arrays.asList("path1"))
            .yellowZonePaths(Arrays.asList("path2"))
            .build();
        
        List<String> errors = invalidConfig.validate();
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("max_files_without_approval")));
    }
    
    @Test
    void testBlastRadiusConfigImmutableLists() {
        // Verificar que las listas son inmutables
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList("path1"))
            .yellowZonePaths(Arrays.asList("path2"))
            .build();
        
        List<String> redPaths = config.getRedZonePaths();
        List<String> yellowPaths = config.getYellowZonePaths();
        
        // Intentar modificar debería lanzar excepción
        assertThrows(UnsupportedOperationException.class, () -> redPaths.add("path3"));
        assertThrows(UnsupportedOperationException.class, () -> yellowPaths.add("path4"));
    }
}
