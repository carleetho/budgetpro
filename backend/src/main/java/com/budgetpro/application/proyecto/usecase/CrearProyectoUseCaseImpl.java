package com.budgetpro.application.proyecto.usecase;

import com.budgetpro.application.proyecto.dto.CrearProyectoCommand;
import com.budgetpro.application.proyecto.dto.ProyectoResponse;
import com.budgetpro.application.proyecto.exception.ProyectoDuplicadoException;
import com.budgetpro.application.proyecto.port.in.CrearProyectoUseCase;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.mapper.ProyectoMapper;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del caso de uso para crear un proyecto.
 */
@Service
public class CrearProyectoUseCaseImpl implements CrearProyectoUseCase {

    private final ProyectoRepository proyectoRepository;
    private final ProyectoMapper proyectoMapper;
    private final ProyectoJpaRepository proyectoJpaRepository;

    public CrearProyectoUseCaseImpl(ProyectoRepository proyectoRepository, 
                                    ProyectoMapper proyectoMapper,
                                    ProyectoJpaRepository proyectoJpaRepository) {
        this.proyectoRepository = proyectoRepository;
        this.proyectoMapper = proyectoMapper;
        this.proyectoJpaRepository = proyectoJpaRepository;
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

        // Obtener la entidad persistida para extraer las fechas (createdAt, updatedAt)
        ProyectoEntity entity = proyectoJpaRepository.findById(id.getValue())
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar el proyecto después de persistirlo"));

        // Retornar respuesta usando el mapper para obtener las fechas
        return proyectoMapper.toResponse(entity);
    }
}
