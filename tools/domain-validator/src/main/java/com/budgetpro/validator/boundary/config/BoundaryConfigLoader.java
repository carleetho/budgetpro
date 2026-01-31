package com.budgetpro.validator.boundary.config;

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
 * Cargador de la configuración de fronteras arquitectónicas. Soporta formatos
 * JSON y YAML.
 */
public class BoundaryConfigLoader {

    private static final String DEFAULT_BOUNDARY_RULES_RESOURCE = "/boundary-rules.json";
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    public BoundaryConfigLoader() {
        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    /**
     * Carga las reglas de frontera desde un archivo externo. Soporta .json y
     * .yaml/.yml.
     */
    public BoundaryConfig loadFromFile(Path configPath) throws BoundaryLoadException {
        File file = configPath.toFile();
        if (!file.exists()) {
            throw new BoundaryLoadException("Configuration file not found: " + configPath);
        }

        try (InputStream is = new FileInputStream(file)) {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
                return parse(is, yamlMapper);
            } else {
                return parse(is, jsonMapper);
            }
        } catch (IOException e) {
            throw new BoundaryLoadException("Failed to read configuration file: " + e.getMessage(), e);
        }
    }

    /**
     * Carga las reglas de frontera desde el recurso predeterminado.
     */
    public BoundaryConfig loadDefault() throws BoundaryLoadException {
        try (InputStream is = getClass().getResourceAsStream(DEFAULT_BOUNDARY_RULES_RESOURCE)) {
            if (is == null) {
                // Si no hay recurso, devolvemos una configuración vacía pero válida
                return BoundaryConfig.defaultConfig();
            }
            return parse(is, jsonMapper);
        } catch (IOException e) {
            throw new BoundaryLoadException("Failed to load default boundary rules: " + e.getMessage(), e);
        }
    }

    private BoundaryConfig parse(InputStream inputStream, ObjectMapper mapper) throws BoundaryLoadException {
        Objects.requireNonNull(inputStream, "inputStream cannot be null");
        try {
            BoundaryConfig config = mapper.readValue(inputStream, BoundaryConfig.class);
            if (config == null) {
                throw new BoundaryLoadException("Boundary configuration is empty");
            }

            List<String> validationErrors = config.validate();
            if (!validationErrors.isEmpty()) {
                throw new BoundaryLoadException(
                        "Boundary configuration validation failed:\n" + String.join("\n", validationErrors));
            }

            return config;
        } catch (Exception e) {
            throw new BoundaryLoadException("Failed to parse boundary configuration: " + e.getMessage(), e);
        }
    }

    public static class BoundaryLoadException extends Exception {
        public BoundaryLoadException(String message) {
            super(message);
        }

        public BoundaryLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
