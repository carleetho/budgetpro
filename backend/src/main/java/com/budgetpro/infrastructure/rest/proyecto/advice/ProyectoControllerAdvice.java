package com.budgetpro.infrastructure.rest.proyecto.advice;

import com.budgetpro.application.proyecto.exception.ProyectoDuplicadoException;
import com.budgetpro.infrastructure.rest.error.ErrorResponses;
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
    public ResponseEntity<ErrorResponses.ErrorResponse> handleProyectoDuplicado(ProyectoDuplicadoException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponses.error(HttpStatus.CONFLICT.value(), "PROYECTO_DUPLICADO", ex.getMessage()));
    }

    /**
     * Maneja errores de validación de argumentos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponses.ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponses.validation(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", errors));
    }

    /**
     * Maneja excepciones de argumentos ilegales.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponses.error(HttpStatus.BAD_REQUEST.value(), "ILLEGAL_ARGUMENT", ex.getMessage()));
    }

    /**
     * Maneja todas las demás excepciones no manejadas.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Error no manejado al procesar petición de proyecto", ex);
        String message = ex.getMessage() != null ? ex.getMessage() : "Error interno del servidor";
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponses.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_SERVER_ERROR", message));
    }
}
