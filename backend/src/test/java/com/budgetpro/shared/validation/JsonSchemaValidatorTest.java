package com.budgetpro.shared.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitario para JsonSchemaValidator.
 * 
 * Verifica que la validación de esquemas JSON funcione correctamente
 * para todos los tipos de snapshots (fechas, duraciones, secuencia, calendarios).
 */
class JsonSchemaValidatorTest {

    private JsonSchemaValidator validator;

    @BeforeEach
    void setUp() {
        validator = new JsonSchemaValidator();
    }

    @Test
    void validateFechasSnapshot_debeAceptarJsonValido() {
        // Given: JSON válido de fechas
        String jsonValido = """
            {
                "programa": {
                    "fechaInicio": "2024-01-01",
                    "fechaFinEstimada": "2024-12-31"
                },
                "actividades": [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "partidaId": "550e8400-e29b-41d4-a716-446655440001",
                        "fechaInicio": "2024-01-15",
                        "fechaFin": "2024-02-15"
                    }
                ]
            }
            """;

        // When/Then: No debe lanzar excepción
        assertDoesNotThrow(() -> validator.validateFechasSnapshot(jsonValido));
    }

    @Test
    void validateFechasSnapshot_debeRechazarJsonInvalido() {
        // Given: JSON inválido (falta campo requerido)
        String jsonInvalido = """
            {
                "programa": {
                    "fechaInicio": "2024-01-01"
                }
            }
            """;

        // When/Then: Debe lanzar IllegalArgumentException
        assertThrows(IllegalArgumentException.class, 
            () -> validator.validateFechasSnapshot(jsonInvalido));
    }

    @Test
    void validateDuracionesSnapshot_debeAceptarJsonValido() {
        // Given: JSON válido de duraciones
        String jsonValido = """
            {
                "duracionTotalDias": 365,
                "actividades": [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "partidaId": "550e8400-e29b-41d4-a716-446655440001",
                        "duracionDias": 30
                    }
                ]
            }
            """;

        // When/Then: No debe lanzar excepción
        assertDoesNotThrow(() -> validator.validateDuracionesSnapshot(jsonValido));
    }

    @Test
    void validateDuracionesSnapshot_debeRechazarDuracionNegativa() {
        // Given: JSON con duración negativa
        String jsonInvalido = """
            {
                "duracionTotalDias": 365,
                "actividades": [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "partidaId": "550e8400-e29b-41d4-a716-446655440001",
                        "duracionDias": -10
                    }
                ]
            }
            """;

        // When/Then: Debe lanzar IllegalArgumentException
        assertThrows(IllegalArgumentException.class, 
            () -> validator.validateDuracionesSnapshot(jsonInvalido));
    }

    @Test
    void validateSecuenciaSnapshot_debeAceptarJsonValido() {
        // Given: JSON válido de secuencia
        String jsonValido = """
            {
                "actividades": [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "partidaId": "550e8400-e29b-41d4-a716-446655440001",
                        "predecesoras": [
                            "550e8400-e29b-41d4-a716-446655440002"
                        ]
                    }
                ]
            }
            """;

        // When/Then: No debe lanzar excepción
        assertDoesNotThrow(() -> validator.validateSecuenciaSnapshot(jsonValido));
    }

    @Test
    void validateSecuenciaSnapshot_debeAceptarPredecesorasVacias() {
        // Given: JSON con lista de predecesoras vacía
        String jsonValido = """
            {
                "actividades": [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "partidaId": "550e8400-e29b-41d4-a716-446655440001",
                        "predecesoras": []
                    }
                ]
            }
            """;

        // When/Then: No debe lanzar excepción
        assertDoesNotThrow(() -> validator.validateSecuenciaSnapshot(jsonValido));
    }

    @Test
    void validateCalendariosSnapshot_debeAceptarJsonValido() {
        // Given: JSON válido de calendarios
        String jsonValido = """
            {
                "calendarios": [],
                "diasFestivos": [],
                "restricciones": []
            }
            """;

        // When/Then: No debe lanzar excepción
        assertDoesNotThrow(() -> validator.validateCalendariosSnapshot(jsonValido));
    }

    @Test
    void validateCalendariosSnapshot_debeRechazarJsonIncompleto() {
        // Given: JSON incompleto (falta campo requerido)
        String jsonInvalido = """
            {
                "calendarios": [],
                "diasFestivos": []
            }
            """;

        // When/Then: Debe lanzar IllegalArgumentException
        assertThrows(IllegalArgumentException.class, 
            () -> validator.validateCalendariosSnapshot(jsonInvalido));
    }

    @Test
    void validate_debeRechazarJsonNulo() {
        // When/Then: Debe lanzar IllegalArgumentException para JSON nulo
        assertThrows(IllegalArgumentException.class, 
            () -> validator.validateFechasSnapshot(null));
        assertThrows(IllegalArgumentException.class, 
            () -> validator.validateDuracionesSnapshot(null));
        assertThrows(IllegalArgumentException.class, 
            () -> validator.validateSecuenciaSnapshot(null));
        assertThrows(IllegalArgumentException.class, 
            () -> validator.validateCalendariosSnapshot(null));
    }

    @Test
    void validate_debeRechazarJsonVacio() {
        // When/Then: Debe lanzar IllegalArgumentException para JSON vacío
        assertThrows(IllegalArgumentException.class, 
            () -> validator.validateFechasSnapshot(""));
        assertThrows(IllegalArgumentException.class, 
            () -> validator.validateDuracionesSnapshot("   "));
    }

    @Test
    void validate_debeRechazarJsonMalformado() {
        // Given: JSON malformado (no es JSON válido)
        String jsonMalformado = "{ esto no es JSON válido }";

        // When/Then: Debe lanzar IllegalArgumentException
        assertThrows(IllegalArgumentException.class, 
            () -> validator.validateFechasSnapshot(jsonMalformado));
    }
}
