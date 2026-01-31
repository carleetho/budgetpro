package com.budgetpro.validator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Cargador de la configuración de máquinas de estado. Soporta formatos JSON y
 * YAML. Usa Jackson para una deserialización robusta y validación estricta.
 */
public class StateMachineConfigLoader {

    private static final String DEFAULT_RULES_RESOURCE = "/state-machine-rules.yml";
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    public StateMachineConfigLoader() {
        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    /**
     * Carga la configuración desde un archivo externo.
     */
    public StateMachineConfig loadFromFile(Path configPath) throws ConfigurationException {
        File file = configPath.toFile();
        if (!file.exists()) {
            throw new ConfigurationException("Configuration file not found: " + configPath);
        }

        try (InputStream is = new FileInputStream(file)) {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
                return parse(is, yamlMapper);
            } else {
                return parse(is, jsonMapper);
            }
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read configuration file: " + e.getMessage(), e);
        }
    }

    /**
     * Carga la configuración desde el recurso predeterminado.
     */
    public StateMachineConfig loadDefault() throws ConfigurationException {
        try (InputStream is = getClass().getResourceAsStream(DEFAULT_RULES_RESOURCE)) {
            if (is == null) {
                return StateMachineConfig.defaultConfig();
            }
            return parse(is, yamlMapper);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load default state machine rules: " + e.getMessage(), e);
        }
    }

    private StateMachineConfig parse(InputStream inputStream, ObjectMapper mapper) throws ConfigurationException {
        Objects.requireNonNull(inputStream, "inputStream cannot be null");
        try {
            StateMachineConfig config = mapper.readValue(inputStream, StateMachineConfig.class);
            if (config == null) {
                throw new ConfigurationException("State machine configuration is empty");
            }

            List<String> validationErrors = config.validate();
            if (!validationErrors.isEmpty()) {
                throw new ConfigurationException(
                        "State machine configuration validation failed:\n" + String.join("\n", validationErrors));
            }

            return config;
        } catch (Exception e) {
            throw new ConfigurationException("Failed to parse state machine configuration: " + e.getMessage(), e);
        }
    }

    public static class ConfigurationException extends Exception {
        public ConfigurationException(String message) {
            super(message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
