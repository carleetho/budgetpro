package com.budgetpro.validator.output;

import com.budgetpro.validator.model.ValidationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Valida que un JSON de reporte de validación cumple con el esquema esperado.
 */
public class JsonSchemaValidator {
    
    private final ObjectMapper objectMapper;
    
    public JsonSchemaValidator() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Valida que un JSON cumple con el esquema de reporte de validación.
     * 
     * @param json JSON a validar
     * @return Lista de errores de validación (vacía si es válido)
     */
    public List<String> validate(String json) {
        List<String> errors = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(json);
            
            // Validar campos requeridos
            validateRequiredField(root, "validation_id", errors);
            validateRequiredField(root, "timestamp", errors);
            validateRequiredField(root, "repository_path", errors);
            validateRequiredField(root, "canonical_version", errors);
            validateRequiredField(root, "status", errors);
            validateRequiredField(root, "violations", errors);
            validateRequiredField(root, "module_statuses", errors);
            
            // Validar que violations es un array
            if (root.has("violations") && !root.get("violations").isArray()) {
                errors.add("Field 'violations' must be an array");
            }
            
            // Validar que module_statuses es un array
            if (root.has("module_statuses") && !root.get("module_statuses").isArray()) {
                errors.add("Field 'module_statuses' must be an array");
            }
            
            // Validar estructura de violations
            if (root.has("violations") && root.get("violations").isArray()) {
                int index = 0;
                for (JsonNode violation : root.get("violations")) {
                    validateViolationStructure(violation, index, errors);
                    index++;
                }
            }
            
            // Validar estructura de module_statuses
            if (root.has("module_statuses") && root.get("module_statuses").isArray()) {
                int index = 0;
                for (JsonNode moduleStatus : root.get("module_statuses")) {
                    validateModuleStatusStructure(moduleStatus, index, errors);
                    index++;
                }
            }
            
        } catch (Exception e) {
            errors.add("Invalid JSON: " + e.getMessage());
        }
        
        return errors;
    }

    /**
     * Valida que un campo requerido existe.
     */
    private void validateRequiredField(JsonNode root, String fieldName, List<String> errors) {
        if (!root.has(fieldName)) {
            errors.add("Missing required field: " + fieldName);
        }
    }

    /**
     * Valida la estructura de un objeto Violation.
     */
    private void validateViolationStructure(JsonNode violation, int index, List<String> errors) {
        String prefix = "violations[" + index + "]";
        
        validateRequiredField(violation, "module_id", errors, prefix);
        validateRequiredField(violation, "severity", errors, prefix);
        validateRequiredField(violation, "type", errors, prefix);
        validateRequiredField(violation, "message", errors, prefix);
        
        // Validar valores de severity
        if (violation.has("severity")) {
            String severity = violation.get("severity").asText();
            if (!severity.equals("CRITICAL") && !severity.equals("WARNING") && !severity.equals("INFO")) {
                errors.add(prefix + ".severity must be one of: CRITICAL, WARNING, INFO");
            }
        }
        
        // Validar valores de type
        if (violation.has("type")) {
            String type = violation.get("type").asText();
            if (!type.equals("STATE_DEPENDENCY") && 
                !type.equals("DATA_DEPENDENCY") && 
                !type.equals("TEMPORAL_DEPENDENCY") && 
                !type.equals("BUSINESS_LOGIC")) {
                errors.add(prefix + ".type must be one of: STATE_DEPENDENCY, DATA_DEPENDENCY, TEMPORAL_DEPENDENCY, BUSINESS_LOGIC");
            }
        }
    }

    /**
     * Valida la estructura de un objeto ModuleStatus.
     */
    private void validateModuleStatusStructure(JsonNode moduleStatus, int index, List<String> errors) {
        String prefix = "module_statuses[" + index + "]";
        
        validateRequiredField(moduleStatus, "module_id", errors, prefix);
        validateRequiredField(moduleStatus, "implementation_status", errors, prefix);
        
        // Validar valores de implementation_status
        if (moduleStatus.has("implementation_status")) {
            String status = moduleStatus.get("implementation_status").asText();
            if (!status.equals("NOT_STARTED") && 
                !status.equals("IN_PROGRESS") && 
                !status.equals("COMPLETE")) {
                errors.add(prefix + ".implementation_status must be one of: NOT_STARTED, IN_PROGRESS, COMPLETE");
            }
        }
    }

    /**
     * Valida que un campo requerido existe (con prefijo).
     */
    private void validateRequiredField(JsonNode node, String fieldName, List<String> errors, String prefix) {
        if (!node.has(fieldName)) {
            errors.add(prefix + "." + fieldName + " is required");
        }
    }
}
