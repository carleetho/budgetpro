package com.budgetpro.validator.boundary.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * Cargador de la configuración de fronteras arquitectónicas.
 */
public class BoundaryConfigLoader {

    private static final String BOUNDARY_RULES_RESOURCE_PATH = "/boundary-rules.json";
    private final ObjectMapper objectMapper;

    public BoundaryConfigLoader() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    /**
     * Carga las reglas de frontera desde el archivo de recursos predeterminado.
     */
    public BoundaryConfig load() throws BoundaryLoadException {
        try (InputStream inputStream = getClass().getResourceAsStream(BOUNDARY_RULES_RESOURCE_PATH)) {
            if (inputStream == null) {
                throw new BoundaryLoadException("Boundary rules resource not found: " + BOUNDARY_RULES_RESOURCE_PATH);
            }
            return load(inputStream);
        } catch (BoundaryLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new BoundaryLoadException("Failed to load boundary rules: " + e.getMessage(), e);
        }
    }

    /**
     * Carga las reglas de frontera desde un InputStream personalizado.
     */
    public BoundaryConfig load(InputStream inputStream) throws BoundaryLoadException {
        Objects.requireNonNull(inputStream, "inputStream cannot be null");

        try {
            BoundaryConfig config = objectMapper.readValue(inputStream, BoundaryConfig.class);

            if (config == null) {
                throw new BoundaryLoadException("Boundary configuration index is empty");
            }

            List<String> validationErrors = config.validate();
            if (!validationErrors.isEmpty()) {
                throw new BoundaryLoadException(
                        "Boundary configuration validation failed:\n" + String.join("\n", validationErrors));
            }

            return config;

        } catch (Exception e) {
            throw new BoundaryLoadException("Failed to parse boundary rules: " + e.getMessage(), e);
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
