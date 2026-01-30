package com.budgetpro.blastradius.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Cargador de configuración para validación de blast radius.
 * Soporta carga desde archivo JSON o uso de configuración por defecto.
 */
public class ConfigLoader {
    
    private static final String DEFAULT_CONFIG_RESOURCE_PATH = "/default-config.json";
    private final ObjectMapper objectMapper;
    
    public ConfigLoader() {
        this.objectMapper = new ObjectMapper();
        // Configurar para aceptar snake_case en JSON
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }
    
    /**
     * Carga la configuración desde un archivo JSON.
     * 
     * @param configFile Ruta al archivo de configuración
     * @return Configuración cargada y validada
     * @throws ConfigLoadException si hay error al cargar o validar la configuración
     */
    public BlastRadiusConfig load(Path configFile) throws ConfigLoadException {
        Objects.requireNonNull(configFile, "configFile cannot be null");
        
        if (!Files.exists(configFile)) {
            throw new ConfigLoadException("Configuration file not found: " + configFile);
        }
        
        if (!Files.isRegularFile(configFile)) {
            throw new ConfigLoadException("Configuration path is not a file: " + configFile);
        }
        
        try (InputStream inputStream = Files.newInputStream(configFile)) {
            BlastRadiusConfig config = objectMapper.readValue(inputStream, BlastRadiusConfig.class);
            
            if (config == null) {
                throw new ConfigLoadException("Configuration file is empty or invalid");
            }
            
            List<String> validationErrors = config.validate();
            if (!validationErrors.isEmpty()) {
                throw new ConfigLoadException(
                    "Configuration validation failed:\n" + String.join("\n", validationErrors));
            }
            
            return config;
            
        } catch (IOException e) {
            throw new ConfigLoadException("Failed to read configuration file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ConfigLoadException("Failed to parse configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Carga la configuración por defecto desde recursos.
     * 
     * @return Configuración por defecto
     * @throws ConfigLoadException si hay error al cargar la configuración por defecto
     */
    public BlastRadiusConfig loadDefaults() throws ConfigLoadException {
        try (InputStream inputStream = getClass().getResourceAsStream(DEFAULT_CONFIG_RESOURCE_PATH)) {
            if (inputStream == null) {
                throw new ConfigLoadException(
                    "Default configuration resource not found: " + DEFAULT_CONFIG_RESOURCE_PATH);
            }
            
            BlastRadiusConfig config = objectMapper.readValue(inputStream, BlastRadiusConfig.class);
            
            if (config == null) {
                throw new ConfigLoadException("Default configuration is empty or invalid");
            }
            
            List<String> validationErrors = config.validate();
            if (!validationErrors.isEmpty()) {
                throw new ConfigLoadException(
                    "Default configuration validation failed:\n" + String.join("\n", validationErrors));
            }
            
            return config;
            
        } catch (IOException e) {
            throw new ConfigLoadException("Failed to read default configuration: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ConfigLoadException("Failed to parse default configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Excepción lanzada cuando hay error al cargar o validar la configuración.
     */
    public static class ConfigLoadException extends Exception {
        public ConfigLoadException(String message) {
            super(message);
        }
        
        public ConfigLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
