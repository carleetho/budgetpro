package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.NominaResponse;
import com.budgetpro.application.rrhh.port.in.ConsultarNominaUseCase;
import com.budgetpro.application.rrhh.port.out.NominaRepositoryPort;
import com.budgetpro.domain.rrhh.model.Nomina;
import com.budgetpro.domain.rrhh.model.NominaId;
import java.util.Optional;

public class ConsultarNominaUseCaseImpl implements ConsultarNominaUseCase {

    private final NominaRepositoryPort nominaRepositoryPort;

    public ConsultarNominaUseCaseImpl(NominaRepositoryPort nominaRepositoryPort) {
        this.nominaRepositoryPort = nominaRepositoryPort;
    }

    @Override
    public Optional<NominaResponse> obtenerPorId(NominaId id) {
        return nominaRepositoryPort.findById(id).map(NominaResponse::fromDomain);
    }
}
