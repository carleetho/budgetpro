package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.NominaResponse;
import com.budgetpro.domain.rrhh.model.NominaId;
import java.util.Optional;

public interface ConsultarNominaUseCase {
    Optional<NominaResponse> obtenerPorId(NominaId id);
}
