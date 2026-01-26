package com.budgetpro.application.estimacion.port.out;

import com.budgetpro.application.estimacion.dto.HorasLaborResponse;
import java.time.LocalDate;
import java.util.UUID;

public interface RRHHIntegrationPort {
    HorasLaborResponse consultarHoras(UUID proyectoId, UUID partidaId, LocalDate inicio, LocalDate fin);
}
