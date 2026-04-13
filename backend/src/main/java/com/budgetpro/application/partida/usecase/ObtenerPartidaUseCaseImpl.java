package com.budgetpro.application.partida.usecase;

import com.budgetpro.application.partida.dto.PartidaResponse;
import com.budgetpro.application.partida.port.in.ObtenerPartidaUseCase;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ObtenerPartidaUseCaseImpl implements ObtenerPartidaUseCase {

    private final PartidaRepository partidaRepository;

    public ObtenerPartidaUseCaseImpl(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PartidaResponse obtenerPorId(UUID partidaId) {
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada: " + partidaId));
        return toResponse(partida);
    }

    static PartidaResponse toResponse(Partida partida) {
        return new PartidaResponse(
                partida.getId().getValue(),
                partida.getPresupuestoId(),
                partida.getPadreId(),
                partida.getItem(),
                partida.getDescripcion(),
                partida.getUnidad(),
                partida.getMetrado(),
                partida.getNivel(),
                partida.getVersion() != null ? partida.getVersion().intValue() : null,
                null,
                null
        );
    }
}

