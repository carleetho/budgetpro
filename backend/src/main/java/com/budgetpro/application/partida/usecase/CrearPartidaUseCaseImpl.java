package com.budgetpro.application.partida.usecase;

import com.budgetpro.application.partida.dto.CrearPartidaCommand;
import com.budgetpro.application.partida.dto.PartidaResponse;
import com.budgetpro.application.partida.exception.PartidaPadreDiferentePresupuestoException;
import com.budgetpro.application.partida.exception.PartidaPadreNoEncontradaException;
import com.budgetpro.application.partida.exception.PresupuestoNoEncontradoException;
import com.budgetpro.application.partida.port.in.CrearPartidaUseCase;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementación del caso de uso para crear una partida.
 */
@Service
public class CrearPartidaUseCaseImpl implements CrearPartidaUseCase {

    private final PartidaRepository partidaRepository;
    private final PresupuestoRepository presupuestoRepository;

    public CrearPartidaUseCaseImpl(PartidaRepository partidaRepository,
                                  PresupuestoRepository presupuestoRepository) {
        this.partidaRepository = partidaRepository;
        this.presupuestoRepository = presupuestoRepository;
    }

    @Override
    @Transactional
    public PartidaResponse crear(CrearPartidaCommand command) {
        // Validar que el presupuesto exista
        PresupuestoId presupuestoId = PresupuestoId.from(command.presupuestoId());
        if (presupuestoRepository.findById(presupuestoId).isEmpty()) {
            throw new PresupuestoNoEncontradoException(command.presupuestoId());
        }

        // Validar padre si se especifica
        Integer nivel = command.nivel();
        if (command.padreId() != null) {
            // Validar que la partida padre exista
            Partida padre = partidaRepository.findById(command.padreId())
                    .orElseThrow(() -> new PartidaPadreNoEncontradaException(command.padreId()));

            // Validar que el padre pertenezca al mismo presupuesto
            if (!padre.getPresupuestoId().equals(command.presupuestoId())) {
                throw new PartidaPadreDiferentePresupuestoException(
                    command.padreId(),
                    command.presupuestoId(),
                    padre.getPresupuestoId()
                );
            }

            // Si no se especifica nivel, calcularlo como nivel del padre + 1
            if (nivel == null) {
                nivel = padre.getNivel() + 1;
            }
        } else {
            // Si no hay padre, debe ser nivel 1
            if (nivel == null) {
                nivel = 1;
            } else if (nivel != 1) {
                throw new IllegalArgumentException("Una partida raíz debe tener nivel 1");
            }
        }

        // Crear la partida
        PartidaId id = PartidaId.nuevo();
        BigDecimal metrado = command.metrado() != null ? command.metrado() : BigDecimal.ZERO;

        Partida partida;
        if (command.padreId() == null) {
            // Partida raíz
            partida = Partida.crearRaiz(id, command.presupuestoId(), command.item(),
                                       command.descripcion(), command.unidad(), metrado);
        } else {
            // Partida hija
            partida = Partida.crearHija(id, command.presupuestoId(), command.padreId(),
                                       command.item(), command.descripcion(), command.unidad(),
                                       metrado, nivel);
        }

        // Persistir
        partidaRepository.save(partida);

        // Retornar respuesta
        return new PartidaResponse(
                partida.getId().getValue(),
                partida.getPresupuestoId(),
                partida.getPadreId(),
                partida.getItem(),
                partida.getDescripcion(),
                partida.getUnidad(),
                partida.getMetrado(),
                partida.getNivel(),
                partida.getVersion().intValue(),
                null, // createdAt se obtiene de la entidad después de persistir
                null  // updatedAt se obtiene de la entidad después de persistir
        );
    }
}
