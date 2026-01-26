package com.budgetpro.shared.validation;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Validador de esquemas JSON para snapshots de cronograma.
 * 
 * Valida que los JSON generados cumplan con los esquemas definidos antes de
 * persistirlos en la base de datos.
 * 
 * **Esquemas soportados:** - fechas-snapshot-schema.json: Estructura de fechas
 * del cronograma - duraciones-snapshot-schema.json: Estructura de duraciones -
 * secuencia-snapshot-schema.json: Estructura de secuencia y dependencias -
 * calendarios-snapshot-schema.json: Estructura de calendarios
 * 
 * **Uso:** ```java validator.validateFechasSnapshot(jsonString); ```
 */
@Component
public class JsonSchemaValidator implements com.budgetpro.domain.shared.port.out.JsonSerializerPort {

    private static final String SCHEMAS_PATH = "schemas/";

    private final Schema fechasSchema;
    private final Schema duracionesSchema;
    private final Schema secuenciaSchema;
    private final Schema calendariosSchema;

    public JsonSchemaValidator() {
        try {
            this.fechasSchema = loadSchema("fechas-snapshot-schema.json");
            this.duracionesSchema = loadSchema("duraciones-snapshot-schema.json");
            this.secuenciaSchema = loadSchema("secuencia-snapshot-schema.json");
            this.calendariosSchema = loadSchema("calendarios-snapshot-schema.json");
        } catch (IOException e) {
            throw new IllegalStateException("Error al cargar esquemas JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Valida el JSON de fechas del snapshot.
     * 
     * @param jsonString JSON string a validar
     * @throws IllegalArgumentException si el JSON no cumple con el esquema
     */
    public void validateFechasSnapshot(String jsonString) {
        validate(jsonString, fechasSchema, "fechas_snapshot");
    }

    /**
     * Valida el JSON de duraciones del snapshot.
     * 
     * @param jsonString JSON string a validar
     * @throws IllegalArgumentException si el JSON no cumple con el esquema
     */
    public void validateDuracionesSnapshot(String jsonString) {
        validate(jsonString, duracionesSchema, "duraciones_snapshot");
    }

    /**
     * Valida el JSON de secuencia del snapshot.
     * 
     * @param jsonString JSON string a validar
     * @throws IllegalArgumentException si el JSON no cumple con el esquema
     */
    public void validateSecuenciaSnapshot(String jsonString) {
        validate(jsonString, secuenciaSchema, "secuencia_snapshot");
    }

    /**
     * Valida el JSON de calendarios del snapshot.
     * 
     * @param jsonString JSON string a validar
     * @throws IllegalArgumentException si el JSON no cumple con el esquema
     */
    public void validateCalendariosSnapshot(String jsonString) {
        validate(jsonString, calendariosSchema, "calendarios_snapshot");
    }

    /**
     * Valida un JSON contra un esquema específico.
     * 
     * @param jsonString JSON string a validar
     * @param schema     Esquema contra el cual validar
     * @param fieldName  Nombre del campo para mensajes de error
     * @throws IllegalArgumentException si el JSON no cumple con el esquema
     */
    private void validate(String jsonString, Schema schema, String fieldName) {
        if (jsonString == null) {
            throw new IllegalArgumentException("El JSON no puede ser nulo");
        }
        if (schema == null) {
            throw new IllegalArgumentException("El esquema no puede ser nulo");
        }

        if (jsonString.isBlank()) {
            throw new IllegalArgumentException(String.format("El JSON de %s no puede estar vacío", fieldName));
        }

        try {
            JSONObject jsonObject = new JSONObject(new JSONTokener(jsonString));
            schema.validate(jsonObject);
        } catch (org.json.JSONException e) {
            throw new IllegalArgumentException(
                    String.format("El JSON de %s no es válido: %s", fieldName, e.getMessage()), e);
        } catch (ValidationException e) {
            String errorMessage = buildValidationErrorMessage(e, fieldName);
            throw new IllegalArgumentException(
                    String.format("El JSON de %s no cumple con el esquema: %s", fieldName, errorMessage), e);
        }
    }

    /**
     * Construye un mensaje de error detallado a partir de ValidationException.
     */
    private String buildValidationErrorMessage(ValidationException e, String fieldName) {
        StringBuilder message = new StringBuilder();
        message.append(e.getMessage());

        if (!e.getCausingExceptions().isEmpty()) {
            message.append(" - Errores: ");
            for (ValidationException causingException : e.getCausingExceptions()) {
                message.append(causingException.getMessage()).append("; ");
            }
        }

        return message.toString();
    }

    /**
     * Carga un esquema JSON desde los recursos.
     * 
     * @param schemaFileName Nombre del archivo del esquema
     * @return Schema cargado
     * @throws IOException si hay error al leer el archivo
     */
    private Schema loadSchema(String schemaFileName) throws IOException {
        ClassPathResource resource = new ClassPathResource(SCHEMAS_PATH + schemaFileName);

        try (InputStream inputStream = resource.getInputStream()) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            return SchemaLoader.load(rawSchema);
        }
    }

    @Override
    public String toJson(Object object) {
        if (object == null) {
            return "{}";
        }
        return new JSONObject(object).toString();
    }

    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        throw new UnsupportedOperationException("Deserialization not implemented in JsonSchemaValidator");
    }
}
