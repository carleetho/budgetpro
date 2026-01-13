package com.budgetpro.application.proyecto.usecase;

import com.budgetpro.application.proyecto.dto.CrearProyectoCommand;
import com.budgetpro.application.proyecto.dto.ProyectoResponse;
import com.budgetpro.application.proyecto.exception.ProyectoDuplicadoException;
import com.budgetpro.application.proyecto.port.in.CrearProyectoUseCase;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del caso de uso para crear un proyecto.
 */
@Service
public class CrearProyectoUseCaseImpl implements CrearProyectoUseCase {

    private final ProyectoRepository proyectoRepository;

    public CrearProyectoUseCaseImpl(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
    }

    @Override
    @Transactional
    public ProyectoResponse crear(CrearProyectoCommand command) {
        // Validar que no exista un proyecto con el mismo nombre
        String nombreNormalizado = command.nombre().trim().toUpperCase();
        if (proyectoRepository.existsByNombre(nombreNormalizado)) {
            throw new ProyectoDuplicadoException(command.nombre());
        }

        // Crear el proyecto
        ProyectoId id = ProyectoId.nuevo();
        Proyecto proyecto = Proyecto.crear(id, command.nombre(), command.ubicacion());

        // Persistir
        proyectoRepository.save(proyecto);

        // Retornar respuesta
        return new ProyectoResponse(
                proyecto.getId().getValue(),
                proyecto.getNombre(),
                proyecto.getUbicacion(),
                proyecto.getEstado(),
                null, // createdAt se obtiene de la entidad después de persistir
                null  // updatedAt se obtiene de la entidad después de persistir
        );
    }
}
