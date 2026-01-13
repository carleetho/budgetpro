package com.budgetpro.application.presupuesto.usecase;

import com.budgetpro.application.presupuesto.dto.CrearPresupuestoCommand;
import com.budgetpro.application.presupuesto.dto.PresupuestoResponse;
import com.budgetpro.application.presupuesto.exception.PresupuestoYaExisteException;
import com.budgetpro.application.presupuesto.exception.ProyectoNoEncontradoException;
import com.budgetpro.application.presupuesto.port.in.CrearPresupuestoUseCase;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del caso de uso para crear un presupuesto.
 */
@Service
public class CrearPresupuestoUseCaseImpl implements CrearPresupuestoUseCase {

    private final PresupuestoRepository presupuestoRepository;
    private final ProyectoRepository proyectoRepository;

    public CrearPresupuestoUseCaseImpl(PresupuestoRepository presupuestoRepository,
                                      ProyectoRepository proyectoRepository) {
        this.presupuestoRepository = presupuestoRepository;
        this.proyectoRepository = proyectoRepository;
    }

    @Override
    @Transactional
    public PresupuestoResponse crear(CrearPresupuestoCommand command) {
        // Validar que el proyecto exista
        ProyectoId proyectoId = ProyectoId.from(command.proyectoId());
        if (proyectoRepository.findById(proyectoId).isEmpty()) {
            throw new ProyectoNoEncontradoException(command.proyectoId());
        }

        // Validar que el proyecto no tenga ya un presupuesto
        if (presupuestoRepository.existsByProyectoId(command.proyectoId())) {
            throw new PresupuestoYaExisteException(command.proyectoId());
        }

        // Crear el presupuesto
        PresupuestoId id = PresupuestoId.nuevo();
        Presupuesto presupuesto = Presupuesto.crear(id, command.proyectoId(), command.nombre());

        // Persistir
        presupuestoRepository.save(presupuesto);

        // Retornar respuesta
        return new PresupuestoResponse(
                presupuesto.getId().getValue(),
                presupuesto.getProyectoId(),
                presupuesto.getNombre(),
                presupuesto.getEstado(),
                presupuesto.getEsContractual(),
                java.math.BigDecimal.ZERO, // Costo total inicial (sin partidas aún)
                presupuesto.getVersion().intValue(),
                null, // createdAt se obtiene de la entidad después de persistir
                null  // updatedAt se obtiene de la entidad después de persistir
        );
    }
}
