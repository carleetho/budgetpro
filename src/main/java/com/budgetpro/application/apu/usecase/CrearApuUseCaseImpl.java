package com.budgetpro.application.apu.usecase;

import com.budgetpro.application.apu.dto.ApuInsumoCommand;
import com.budgetpro.application.apu.dto.ApuInsumoResponse;
import com.budgetpro.application.apu.dto.CrearApuCommand;
import com.budgetpro.application.apu.dto.ApuResponse;
import com.budgetpro.application.apu.exception.ApuYaExisteException;
import com.budgetpro.application.apu.exception.PartidaNoEncontradaException;
import com.budgetpro.application.apu.exception.RecursoNoEncontradoException;
import com.budgetpro.application.apu.port.in.CrearApuUseCase;
import com.budgetpro.domain.finanzas.apu.model.APU;
import com.budgetpro.domain.finanzas.apu.model.ApuId;
import com.budgetpro.domain.finanzas.apu.model.ApuInsumo;
import com.budgetpro.domain.finanzas.apu.port.out.ApuRepository;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.application.recurso.port.out.RecursoRepository;
import com.budgetpro.domain.recurso.model.RecursoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para crear un APU.
 */
@Service
public class CrearApuUseCaseImpl implements CrearApuUseCase {

    private final ApuRepository apuRepository;
    private final PartidaRepository partidaRepository;
    private final RecursoRepository recursoRepository;

    public CrearApuUseCaseImpl(ApuRepository apuRepository,
                               PartidaRepository partidaRepository,
                               RecursoRepository recursoRepository) {
        this.apuRepository = apuRepository;
        this.partidaRepository = partidaRepository;
        this.recursoRepository = recursoRepository;
    }

    @Override
    @Transactional
    public ApuResponse crear(CrearApuCommand command) {
        // Validar que la partida exista
        PartidaId partidaId = PartidaId.from(command.partidaId());
        if (partidaRepository.findById(partidaId).isEmpty()) {
            throw new PartidaNoEncontradaException(command.partidaId());
        }

        // Validar que la partida no tenga ya un APU
        if (apuRepository.existsByPartidaId(command.partidaId())) {
            throw new ApuYaExisteException(command.partidaId());
        }

        // Validar que todos los recursos existan
        for (ApuInsumoCommand insumoCommand : command.insumos()) {
            RecursoId recursoId = RecursoId.of(insumoCommand.recursoId());
            if (recursoRepository.findById(recursoId).isEmpty()) {
                throw new RecursoNoEncontradoException(insumoCommand.recursoId());
            }
        }

        // Crear el APU
        ApuId id = ApuId.nuevo();
        APU apu;
        
        if (command.rendimiento() != null) {
            apu = APU.crear(id, command.partidaId(), command.rendimiento(), command.unidad());
        } else {
            apu = APU.crear(id, command.partidaId(), command.unidad());
        }

        // Agregar insumos al APU
        for (ApuInsumoCommand insumoCommand : command.insumos()) {
            apu.agregarInsumo(
                insumoCommand.recursoId(),
                insumoCommand.cantidad(),
                insumoCommand.precioUnitario()
            );
        }

        // Persistir
        apuRepository.save(apu);

        // Mapear insumos a respuesta
        List<ApuInsumoResponse> insumosResponse = apu.getInsumos().stream()
                .map(insumo -> new ApuInsumoResponse(
                    insumo.getId().getValue(),
                    insumo.getRecursoId(),
                    insumo.getCantidad(),
                    insumo.getPrecioUnitario(),
                    insumo.getSubtotal()
                ))
                .collect(Collectors.toList());

        // Retornar respuesta
        return new ApuResponse(
                apu.getId().getValue(),
                apu.getPartidaId(),
                apu.getRendimiento(),
                apu.getUnidad(),
                apu.calcularCostoTotal(),
                apu.getVersion().intValue(),
                insumosResponse,
                null, // createdAt se obtiene de la entidad después de persistir
                null  // updatedAt se obtiene de la entidad después de persistir
        );
    }
}
