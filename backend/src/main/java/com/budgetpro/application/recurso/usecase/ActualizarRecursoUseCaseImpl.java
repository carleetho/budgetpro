package com.budgetpro.application.recurso.usecase;

import com.budgetpro.application.recurso.dto.ActualizarRecursoCommand;
import com.budgetpro.application.recurso.dto.RecursoResponse;
import com.budgetpro.application.recurso.port.in.ActualizarRecursoUseCase;
import com.budgetpro.application.recurso.port.out.RecursoRepository;
import com.budgetpro.domain.finanzas.recurso.model.EstadoRecurso;
import com.budgetpro.domain.finanzas.recurso.model.Recurso;
import com.budgetpro.domain.finanzas.recurso.model.RecursoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ActualizarRecursoUseCaseImpl implements ActualizarRecursoUseCase {

    private final RecursoRepository recursoRepository;

    public ActualizarRecursoUseCaseImpl(RecursoRepository recursoRepository) {
        this.recursoRepository = recursoRepository;
    }

    @Override
    @Transactional
    public RecursoResponse actualizar(ActualizarRecursoCommand command) {
        if (command == null || command.id() == null) {
            throw new IllegalArgumentException("id es obligatorio");
        }

        UUID id = command.id();
        Recurso recurso = recursoRepository.findById(RecursoId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado: " + id));

        Recurso updated = recurso;
        if (command.nombre() != null) {
            updated = updated.actualizarNombre(command.nombre());
        }
        if (command.unidadBase() != null) {
            updated = updated.actualizarUnidadBase(command.unidadBase());
        }
        if (command.atributos() != null) {
            updated = updated.actualizarAtributos(command.atributos());
        }
        if (command.estado() != null) {
            EstadoRecurso estado = EstadoRecurso.valueOf(command.estado().toUpperCase());
            updated = switch (estado) {
                case ACTIVO -> updated.activar();
                case DEPRECADO -> updated.desactivar();
                case EN_REVISION -> updated.marcarEnRevision();
            };
        }

        recursoRepository.save(updated);
        return ObtenerRecursoUseCaseImpl.toResponse(updated);
    }
}

