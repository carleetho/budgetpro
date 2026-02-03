package com.budgetpro.application.recurso.usecase;

import com.budgetpro.application.recurso.dto.CrearRecursoCommand;
import com.budgetpro.application.recurso.dto.RecursoResponse;
import com.budgetpro.application.recurso.exception.RecursoDuplicadoException;
import com.budgetpro.application.recurso.port.in.CrearRecursoUseCase;
import com.budgetpro.application.recurso.port.out.RecursoRepository;
import com.budgetpro.domain.finanzas.recurso.model.Recurso;
import com.budgetpro.domain.finanzas.recurso.model.RecursoId;
import com.budgetpro.domain.shared.model.TipoRecurso;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Implementación del caso de uso para crear un nuevo recurso.
 * 
 * Responsabilidades: - Orquestar el flujo de creación de recursos - Validar
 * reglas de aplicación (duplicados, etc.) - Coordinar entre el dominio y la
 * persistencia - Controlar transacciones
 * 
 * NO contiene lógica de negocio profunda (eso está en el Agregado Recurso).
 */
@Service
@Validated
@Transactional
public class CrearRecursoUseCaseImpl implements CrearRecursoUseCase {

    private final RecursoRepository recursoRepository;

    public CrearRecursoUseCaseImpl(RecursoRepository recursoRepository) {
        this.recursoRepository = recursoRepository;
    }

    @Override
    public RecursoResponse ejecutar(CrearRecursoCommand command) {
        // 1. Normalizar el nombre para verificar duplicados
        String nombreNormalizado = normalizarNombre(command.nombre());

        // 2. Verificar si ya existe un recurso con el mismo nombre normalizado
        if (recursoRepository.existsByNombre(nombreNormalizado)) {
            throw new RecursoDuplicadoException(nombreNormalizado);
        }

        // 3. Convertir el tipo de String a Enum del dominio
        TipoRecurso tipoRecurso = parsearTipoRecurso(command.tipo());

        // 4. Generar ID único para el nuevo recurso
        RecursoId nuevoId = RecursoId.generate();

        // 5. Crear el agregado usando el factory method apropiado del dominio
        Recurso nuevoRecurso;
        if (command.esProvisional()) {
            // Usar factory method para recurso provisional (estado EN_REVISION)
            nuevoRecurso = Recurso.crearProvisional(nuevoId, command.nombre(), // El dominio normalizará el nombre
                                                                               // automáticamente
                    tipoRecurso, command.unidadBase());
        } else {
            // Usar factory method para recurso normal (estado ACTIVO)
            nuevoRecurso = Recurso.crear(nuevoId, command.nombre(), // El dominio normalizará el nombre automáticamente
                    tipoRecurso, command.unidadBase());
        }

        // 6. Agregar atributos adicionales si existen
        if (command.atributos() != null && !command.atributos().isEmpty()) {
            nuevoRecurso.actualizarAtributos(command.atributos());
        }

        // 7. Persistir el agregado usando el repositorio
        recursoRepository.save(nuevoRecurso);

        // 8. Convertir el agregado a DTO de respuesta
        return toResponse(nuevoRecurso);
    }

    /**
     * Normaliza el nombre del recurso según las reglas de negocio. Usa la misma
     * lógica que el dominio (Trim + UpperCase + espacios múltiples).
     * 
     * @param nombre El nombre a normalizar
     * @return El nombre normalizado
     */
    private String normalizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del recurso no puede estar vacío");
        }
        return nombre.trim().toUpperCase().replaceAll("\\s+", " ");
    }

    /**
     * Convierte un String a TipoRecurso (Enum del dominio).
     * 
     * @param tipoStr El tipo como String
     * @return El TipoRecurso correspondiente
     * @throws IllegalArgumentException si el tipo no es válido
     */
    private TipoRecurso parsearTipoRecurso(String tipoStr) {
        if (tipoStr == null || tipoStr.isBlank()) {
            throw new IllegalArgumentException("El tipo del recurso no puede estar vacío");
        }
        try {
            return TipoRecurso.valueOf(tipoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de recurso inválido: " + tipoStr + ". Valores válidos: "
                    + java.util.Arrays.toString(TipoRecurso.values()), e);
        }
    }

    /**
     * Convierte un agregado del dominio a DTO de respuesta.
     * 
     * @param recurso El agregado del dominio
     * @return El DTO de respuesta
     */
    private RecursoResponse toResponse(Recurso recurso) {
        return new RecursoResponse(recurso.getId().getValue(), recurso.getNombre(), recurso.getTipo().name(),
                recurso.getEstado().name());
    }
}
