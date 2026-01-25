package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.CuadrillaResponse;
import com.budgetpro.domain.rrhh.model.CuadrillaId;

import java.util.List;
import java.util.Optional;

public interface ConsultarCuadrillaUseCase {
    Optional<CuadrillaResponse> findById(CuadrillaId id);

    List<CuadrillaResponse> findAll();
}
