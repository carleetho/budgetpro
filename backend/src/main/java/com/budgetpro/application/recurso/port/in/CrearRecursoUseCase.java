package com.budgetpro.application.recurso.port.in;

import com.budgetpro.application.recurso.dto.CrearRecursoCommand;
import com.budgetpro.application.recurso.dto.RecursoResponse;
import jakarta.validation.Valid;

/**
 * Puerto de Entrada (Inbound Port) para el caso de uso de creación de recursos.
 * 
 * Define el contrato que deben cumplir las implementaciones del caso de uso.
 * Sigue el patrón de Arquitectura Hexagonal.
 */
public interface CrearRecursoUseCase {

    /**
     * Ejecuta el caso de uso para crear un nuevo recurso.
     * 
     * La validación del comando se define en el contrato de la interfaz usando {@link Valid}.
     * Esto asegura que todas las implementaciones validen el comando de manera consistente.
     * 
     * @param command El comando con los datos del recurso a crear (validado automáticamente)
     * @return La respuesta con los datos del recurso creado
     * @throws com.budgetpro.application.recurso.exception.RecursoDuplicadoException si ya existe un recurso con el mismo nombre normalizado
     * @throws jakarta.validation.ConstraintViolationException si los datos del comando no son válidos
     */
    RecursoResponse ejecutar(@Valid CrearRecursoCommand command);
}
