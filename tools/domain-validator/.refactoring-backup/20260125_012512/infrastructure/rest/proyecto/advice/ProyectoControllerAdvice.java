package com.budgetpro.infrastructure.rest.proyecto.advice;

import com.budgetpro.application.proyecto.exception.ProyectoDuplicadoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para ProyectoController.
 * Convierte excepciones de dominio en respuestas HTTP apropiadas.
 */
@RestControllerAdvice(basePackages = "com.budgetpro.infrastructure.rest.proyecto")
public class ProyectoControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ProyectoControllerAdvice.class);

    /**
     * Maneja excepciones de proyecto duplicado.
     */
    @ExceptionHandler(ProyectoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleProyectoDuplicado(ProyectoDuplicadoException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("error", "PROYECTO_DUPLICADO");
        body.put("status", HttpStatus.CONFLICT.value());
        
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }

    /**
     * Maneja errores de validación de argumentos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Error de validación en los datos enviados");
        body.put("error", "VALIDATION_ERROR");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        body.put("errors", errors);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    /**
     * Maneja excepciones de argumentos ilegales.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("error", "ILLEGAL_ARGUMENT");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    /**
     * Maneja todas las demás excepciones no manejadas.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Error no manejado al procesar petición de proyecto", ex);
        
        Map<String, Object> body = new HashMap<>();
        String message = ex.getMessage() != null ? ex.getMessage() : "Error interno del servidor";
        body.put("message", message);
        body.put("error", "INTERNAL_SERVER_ERROR");
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("exceptionType", ex.getClass().getSimpleName());
        
        // En desarrollo, incluir el stack trace (remover en producción)
        if (ex.getCause() != null) {
            body.put("cause", ex.getCause().getMessage());
        }
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}
