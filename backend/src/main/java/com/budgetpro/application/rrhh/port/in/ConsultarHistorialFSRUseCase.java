package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.HistorialFSRResponse;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import java.time.LocalDate;

public interface ConsultarHistorialFSRUseCase {
    HistorialFSRResponse consultarHistorial(ProyectoId proyectoId, LocalDate fechaInicio, LocalDate fechaFin);
}
