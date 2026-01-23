package com.budgetpro.validator.output;

import com.budgetpro.validator.model.ValidationResult;
import com.budgetpro.validator.roadmap.CanonicalRoadmap;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Genera reportes JSON de validación en formato machine-readable.
 */
public class JsonReportGenerator {
    
    private static final DateTimeFormatter ISO_8601_FORMATTER = 
        DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);
    
    private final ObjectMapper objectMapper;
    
    public JsonReportGenerator() {
        this.objectMapper = new ObjectMapper();
        // Configurar para pretty print
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Incluir solo campos no nulos
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // Escribir fechas como ISO 8601
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Genera un reporte JSON completo desde un ValidationResult.
     * 
     * @param result Resultado de validación
     * @param roadmap Roadmap canónico usado para la validación
     * @return JSON como string
     * @throws IOException si hay error al serializar
     */
    public String generate(ValidationResult result, CanonicalRoadmap roadmap) throws IOException {
        // Asegurar que el resultado tenga todos los metadatos necesarios
        enrichResult(result, roadmap);
        
        // Serializar a JSON
        return objectMapper.writeValueAsString(result);
    }

    /**
     * Genera un reporte JSON y lo escribe a un archivo.
     * 
     * @param result Resultado de validación
     * @param roadmap Roadmap canónico usado para la validación
     * @param outputPath Ruta del archivo de salida
     * @throws IOException si hay error al escribir
     */
    public void generateToFile(ValidationResult result, CanonicalRoadmap roadmap, Path outputPath) throws IOException {
        String json = generate(result, roadmap);
        
        // Crear directorio padre si no existe
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        
        // Escribir archivo
        Files.writeString(outputPath, json);
    }

    /**
     * Genera un reporte JSON y lo escribe a un OutputStream.
     * 
     * @param result Resultado de validación
     * @param roadmap Roadmap canónico usado para la validación
     * @param outputStream Stream de salida
     * @throws IOException si hay error al escribir
     */
    public void generateToStream(ValidationResult result, CanonicalRoadmap roadmap, OutputStream outputStream) throws IOException {
        enrichResult(result, roadmap);
        objectMapper.writeValue(outputStream, result);
    }

    /**
     * Genera un reporte JSON y lo escribe a un Writer.
     * 
     * @param result Resultado de validación
     * @param roadmap Roadmap canónico usado para la validación
     * @param writer Writer de salida
     * @throws IOException si hay error al escribir
     */
    public void generateToWriter(ValidationResult result, CanonicalRoadmap roadmap, Writer writer) throws IOException {
        enrichResult(result, roadmap);
        objectMapper.writeValue(writer, result);
    }

    /**
     * Enriquece el ValidationResult con metadatos adicionales si faltan.
     */
    private void enrichResult(ValidationResult result, CanonicalRoadmap roadmap) {
        // Asegurar que tiene validation_id
        if (result.getValidationId() == null || result.getValidationId().isEmpty()) {
            result.setValidationId(UUID.randomUUID().toString());
        }
        
        // Asegurar que tiene timestamp en formato ISO 8601
        if (result.getTimestamp() == null || result.getTimestamp().isEmpty()) {
            result.setTimestamp(ISO_8601_FORMATTER.format(Instant.now()));
        } else {
            // Convertir timestamp existente a ISO 8601 si no está en ese formato
            try {
                // Si ya está en formato ISO, dejarlo como está
                Instant.parse(result.getTimestamp());
            } catch (Exception e) {
                // Si no está en formato ISO, convertir
                result.setTimestamp(ISO_8601_FORMATTER.format(Instant.now()));
            }
        }
        
        // Asegurar que tiene canonical_version del roadmap
        if (result.getCanonicalVersion() == null || result.getCanonicalVersion().isEmpty()) {
            if (roadmap != null) {
                result.setCanonicalVersion(roadmap.getVersion());
            } else {
                result.setCanonicalVersion("1.0.0");
            }
        }
    }

    /**
     * Valida que el JSON generado es válido parseándolo de nuevo.
     * 
     * @param json JSON a validar
     * @return true si el JSON es válido
     */
    public boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
