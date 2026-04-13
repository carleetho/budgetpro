package com.budgetpro.application.recurso.usecase;

import com.budgetpro.application.recurso.dto.RecursoResponse;
import com.budgetpro.application.recurso.port.in.ObtenerRecursoUseCase;
import com.budgetpro.application.recurso.port.out.RecursoRepository;
import com.budgetpro.domain.finanzas.recurso.model.Recurso;
import com.budgetpro.domain.finanzas.recurso.model.RecursoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ObtenerRecursoUseCaseImpl implements ObtenerRecursoUseCase {

    private final RecursoRepository recursoRepository;

    public ObtenerRecursoUseCaseImpl(RecursoRepository recursoRepository) {
        this.recursoRepository = recursoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public RecursoResponse obtenerPorId(UUID id) {
        Recurso recurso = recursoRepository.findById(RecursoId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado: " + id));
        return toResponse(recurso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecursoResponse> listar() {
        return recursoRepository.findAll().stream().map(ObtenerRecursoUseCaseImpl::toResponse).toList();
    }

    static RecursoResponse toResponse(Recurso recurso) {
        return new RecursoResponse(
                recurso.getId().getValue(),
                recurso.getNombre(),
                recurso.getTipo().name(),
                recurso.getEstado().name()
        );
    }
}

