package com.budgetpro.application.avance.usecase;

import com.budgetpro.application.avance.dto.AvanceFisicoResponse;
import com.budgetpro.application.avance.dto.RegistrarAvanceCommand;
import com.budgetpro.application.avance.port.in.RegistrarAvanceUseCase;
import com.budgetpro.application.compra.exception.PartidaNoEncontradaException;
import com.budgetpro.domain.finanzas.avance.model.AvanceFisico;
import com.budgetpro.domain.finanzas.avance.port.out.AvanceFisicoRepository;
import com.budgetpro.domain.finanzas.avance.service.ControlAvanceService;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementación del caso de uso para registrar un avance físico.
 */
@Service
public class RegistrarAvanceUseCaseImpl implements RegistrarAvanceUseCase {

    private final PartidaRepository partidaRepository;
    private final AvanceFisicoRepository avanceFisicoRepository;
    private final ControlAvanceService controlAvanceService;

    public RegistrarAvanceUseCaseImpl(PartidaRepository partidaRepository,
                                      AvanceFisicoRepository avanceFisicoRepository,
                                      ControlAvanceService controlAvanceService) {
        this.partidaRepository = partidaRepository;
        this.avanceFisicoRepository = avanceFisicoRepository;
        this.controlAvanceService = controlAvanceService;
    }

    @Override
    @Transactional
    public AvanceFisicoResponse registrar(RegistrarAvanceCommand command) {
        // 1. Validar que la partida existe
        Partida partida = partidaRepository.findById(PartidaId.from(command.partidaId()))
                .orElseThrow(() -> new PartidaNoEncontradaException(command.partidaId()));

        // 2. Registrar el avance usando el servicio de dominio
        AvanceFisico avance = controlAvanceService.registrarAvance(
                partida,
                command.metradoEjecutado(),
                command.fecha(),
                command.observacion()
        );

        // 3. Persistir el avance
        avanceFisicoRepository.save(avance);

        // 4. Calcular el porcentaje de avance actualizado
        BigDecimal porcentajeAvance = controlAvanceService.calcularPorcentajeAvance(partida);

        // 5. Retornar respuesta
        return new AvanceFisicoResponse(
            avance.getId().getValue(),
            avance.getPartidaId(),
            avance.getFecha(),
            avance.getMetradoEjecutado(),
            avance.getObservacion(),
            porcentajeAvance,
            avance.getVersion().intValue()
        );
    }
}
