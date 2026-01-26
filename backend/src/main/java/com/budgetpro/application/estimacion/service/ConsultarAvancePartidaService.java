package com.budgetpro.application.estimacion.service;

import com.budgetpro.application.estimacion.dto.AvancePartidaResponse;
import com.budgetpro.application.estimacion.port.in.ConsultarAvancePartidaUseCase;
import com.budgetpro.domain.finanzas.estimacion.port.AvancePartidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConsultarAvancePartidaService implements ConsultarAvancePartidaUseCase {

    private final AvancePartidaRepository avancePartidaRepository;

    public ConsultarAvancePartidaService(AvancePartidaRepository avancePartidaRepository) {
        this.avancePartidaRepository = avancePartidaRepository;
    }

    @Override
    public AvancePartidaResponse consultar(UUID partidaId, UUID proyectoId) {
        // Note: Repository methods might differ. calculateAvanceAcumulado was defined
        // in adapter.
        // Assuming we rely on that.
        BigDecimal avance = avancePartidaRepository.calcularAvanceAcumulado(partidaId);
        if (avance == null) {
            avance = BigDecimal.ZERO;
        }
        return new AvancePartidaResponse(partidaId, avance);
    }
}
