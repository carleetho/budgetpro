package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.CuadrillaResponse;
import com.budgetpro.application.rrhh.port.in.ConsultarCuadrillaUseCase;
import com.budgetpro.application.rrhh.port.out.CuadrillaRepositoryPort;
import com.budgetpro.domain.rrhh.model.Cuadrilla;
import com.budgetpro.domain.rrhh.model.CuadrillaId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConsultarCuadrillaUseCaseImpl implements ConsultarCuadrillaUseCase {

    private final CuadrillaRepositoryPort repository;

    public ConsultarCuadrillaUseCaseImpl(CuadrillaRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CuadrillaResponse> findById(CuadrillaId id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Override
    public List<CuadrillaResponse> findAll() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private CuadrillaResponse mapToResponse(Cuadrilla cuadrilla) {
        List<UUID> memberIds = cuadrilla.getMiembros().stream()
                .map(m -> m.getEmpleadoId().getValue())
                .collect(Collectors.toList());

        return new CuadrillaResponse(
                cuadrilla.getId().getValue(),
                cuadrilla.getProyectoId().getValue(),
                cuadrilla.getNombre(),
                cuadrilla.getTipo(),
                cuadrilla.getLiderId() != null ? cuadrilla.getLiderId().getValue() : null,
                cuadrilla.getEstado(),
                memberIds);
    }
}
