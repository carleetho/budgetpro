package com.budgetpro.validator.roadmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * Cargador del roadmap canónico desde archivo JSON.
 */
public class RoadmapLoader {
    
    private static final String ROADMAP_RESOURCE_PATH = "/canonical-roadmap.json";
    private final ObjectMapper objectMapper;

    public RoadmapLoader() {
        this.objectMapper = new ObjectMapper();
        // Configurar para aceptar snake_case y camelCase
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    }

    /**
     * Carga el roadmap canónico desde el archivo de recursos.
     * 
     * @return El roadmap canónico cargado
     * @throws RoadmapLoadException si hay error al cargar o validar el roadmap
     */
    public CanonicalRoadmap load() throws RoadmapLoadException {
        try {
            InputStream inputStream = getClass().getResourceAsStream(ROADMAP_RESOURCE_PATH);
            if (inputStream == null) {
                throw new RoadmapLoadException("Roadmap resource not found: " + ROADMAP_RESOURCE_PATH);
            }

            // Leer el JSON completo
            RoadmapWrapper wrapper = objectMapper.readValue(inputStream, RoadmapWrapper.class);
            
            if (wrapper == null || wrapper.getRoadmap() == null) {
                throw new RoadmapLoadException("Roadmap structure is invalid: missing 'roadmap' property");
            }

            CanonicalRoadmap roadmap = wrapper.getRoadmap();
            
            // Validar estructura
            List<String> validationErrors = roadmap.validate();
            if (!validationErrors.isEmpty()) {
                throw new RoadmapLoadException("Roadmap validation failed:\n" + 
                    String.join("\n", validationErrors));
            }

            return roadmap;
            
        } catch (RoadmapLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new RoadmapLoadException("Failed to load roadmap: " + e.getMessage(), e);
        }
    }

    /**
     * Carga el roadmap desde un InputStream personalizado (útil para testing).
     */
    public CanonicalRoadmap load(InputStream inputStream) throws RoadmapLoadException {
        Objects.requireNonNull(inputStream, "inputStream cannot be null");
        
        try {
            RoadmapWrapper wrapper = objectMapper.readValue(inputStream, RoadmapWrapper.class);
            
            if (wrapper == null || wrapper.getRoadmap() == null) {
                throw new RoadmapLoadException("Roadmap structure is invalid: missing 'roadmap' property");
            }

            CanonicalRoadmap roadmap = wrapper.getRoadmap();
            
            // Validar estructura
            List<String> validationErrors = roadmap.validate();
            if (!validationErrors.isEmpty()) {
                throw new RoadmapLoadException("Roadmap validation failed:\n" + 
                    String.join("\n", validationErrors));
            }

            return roadmap;
            
        } catch (RoadmapLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new RoadmapLoadException("Failed to load roadmap: " + e.getMessage(), e);
        }
    }

    /**
     * Wrapper interno para la estructura JSON del roadmap.
     */
    private static class RoadmapWrapper {
        private CanonicalRoadmap roadmap;

        public CanonicalRoadmap getRoadmap() {
            return roadmap;
        }

        public void setRoadmap(CanonicalRoadmap roadmap) {
            this.roadmap = roadmap;
        }
    }

    /**
     * Excepción lanzada cuando hay error al cargar el roadmap.
     */
    public static class RoadmapLoadException extends Exception {
        public RoadmapLoadException(String message) {
            super(message);
        }

        public RoadmapLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
