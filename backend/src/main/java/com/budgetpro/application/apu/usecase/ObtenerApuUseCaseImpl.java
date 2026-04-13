package com.budgetpro.application.apu.usecase;

import com.budgetpro.application.apu.dto.ApuInsumoResponse;
import com.budgetpro.application.apu.dto.ApuResponse;
import com.budgetpro.application.apu.port.in.ObtenerApuUseCase;
import com.budgetpro.domain.finanzas.apu.model.APU;
import com.budgetpro.domain.finanzas.apu.model.ApuId;
import com.budgetpro.domain.finanzas.apu.port.out.ApuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ObtenerApuUseCaseImpl implements ObtenerApuUseCase {

    private final ApuRepository apuRepository;

    public ObtenerApuUseCaseImpl(ApuRepository apuRepository) {
        this.apuRepository = apuRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ApuResponse obtenerPorId(UUID apuId) {
        APU apu = apuRepository.findById(ApuId.from(apuId))
                .orElseThrow(() -> new IllegalArgumentException("APU no encontrado: " + apuId));
        return toResponse(apu);
    }

    @Override
    @Transactional(readOnly = true)
    public ApuResponse obtenerPorPartidaId(UUID partidaId) {
        APU apu = apuRepository.findByPartidaId(partidaId)
                .orElseThrow(() -> new IllegalArgumentException("APU no encontrado para partida: " + partidaId));
        return toResponse(apu);
    }

    static ApuResponse toResponse(APU apu) {
        var insumos = apu.getInsumos().stream()
                .map(i -> new ApuInsumoResponse(
                        i.getId().getValue(),
                        i.getRecursoId(),
                        i.getCantidad(),
                        i.getPrecioUnitario(),
                        i.getSubtotal()
                ))
                .toList();

        return new ApuResponse(
                apu.getId().getValue(),
                apu.getPartidaId(),
                apu.getRendimiento(),
                apu.getUnidad(),
                apu.calcularCostoTotal(),
                apu.getVersion() != null ? apu.getVersion().intValue() : null,
                insumos,
                null,
                null
        );
    }
}

