package com.budgetpro.application.cronograma.usecase;

import com.budgetpro.application.cronograma.dto.ActividadProgramadaResponse;
import com.budgetpro.application.cronograma.dto.ProgramarActividadCommand;
import com.budgetpro.application.cronograma.port.in.ProgramarActividadUseCase;
import com.budgetpro.application.compra.exception.PartidaNoEncontradaException;
import com.budgetpro.application.presupuesto.exception.ProyectoNoEncontradoException;
import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada;
import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramadaId;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;
import com.budgetpro.domain.finanzas.cronograma.port.out.ActividadProgramadaRepository;
import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.domain.finanzas.cronograma.service.CalculoCronogramaService;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementación del caso de uso para programar o actualizar una actividad.
 */
@Service
public class ProgramarActividadUseCaseImpl implements ProgramarActividadUseCase {

    private final ProyectoRepository proyectoRepository;
    private final PartidaRepository partidaRepository;
    private final ProgramaObraRepository programaObraRepository;
    private final ActividadProgramadaRepository actividadProgramadaRepository;
    private final CalculoCronogramaService calculoCronogramaService;

    public ProgramarActividadUseCaseImpl(ProyectoRepository proyectoRepository, PartidaRepository partidaRepository,
            ProgramaObraRepository programaObraRepository, ActividadProgramadaRepository actividadProgramadaRepository,
            CalculoCronogramaService calculoCronogramaService) {
        this.proyectoRepository = proyectoRepository;
        this.partidaRepository = partidaRepository;
        this.programaObraRepository = programaObraRepository;
        this.actividadProgramadaRepository = actividadProgramadaRepository;
        this.calculoCronogramaService = calculoCronogramaService;
    }

    @Override
    @Transactional
    public ActividadProgramadaResponse programar(ProgramarActividadCommand command) {
        // 1. Validar que el proyecto existe
        if (proyectoRepository.findById(ProyectoId.from(command.proyectoId())).isEmpty()) {
            throw new ProyectoNoEncontradoException(command.proyectoId());
        }

        // 2. Validar que la partida existe
        if (partidaRepository.findById(PartidaId.from(command.partidaId())).isEmpty()) {
            throw new PartidaNoEncontradaException(command.partidaId());
        }

        // 3. Buscar o crear el programa de obra del proyecto
        ProgramaObra programaObra = programaObraRepository.findByProyectoId(command.proyectoId()).orElseGet(() -> {
            ProgramaObraId id = ProgramaObraId.nuevo();
            return ProgramaObra.crear(id, command.proyectoId(), command.fechaInicio(), command.fechaFin());
        });

        // Si el programa no tiene fecha de inicio, establecerla desde la primera
        // actividad
        if (programaObra.getFechaInicio() == null && command.fechaInicio() != null) {
            programaObra = programaObra.actualizarFechas(command.fechaInicio(), command.fechaFin());
        }

        // 4. Buscar o crear la actividad programada
        ActividadProgramada actividad = actividadProgramadaRepository.findByPartidaId(command.partidaId())
                .orElseGet(() -> {
                    ActividadProgramadaId id = ActividadProgramadaId.nuevo();
                    return ActividadProgramada.crear(id, command.partidaId(), programaObra.getId().getValue(),
                            command.fechaInicio(), command.fechaFin());
                });

        // 5. Actualizar fechas de la actividad
        actividad = actividad.actualizarFechas(command.fechaInicio(), command.fechaFin());

        // 6. Actualizar predecesoras
        if (command.predecesoras() != null) {
            // Eliminar todas las predecesoras actuales y agregar las nuevas
            List<UUID> predecesorasActuales = actividad.getPredecesoras();
            for (UUID predecesoraId : predecesorasActuales) {
                actividad = actividad.eliminarPredecesora(predecesoraId);
            }
            for (UUID predecesoraId : command.predecesoras()) {
                actividad = actividad.agregarPredecesora(predecesoraId);
            }
        }

        // 7. Persistir actividad
        actividadProgramadaRepository.save(actividad);

        // 8. Recalcular fecha de fin del programa basándose en todas las actividades
        List<ActividadProgramada> todasLasActividades = actividadProgramadaRepository
                .findByProgramaObraId(programaObra.getId().getValue());
        java.time.LocalDate fechaFinMasTardia = calculoCronogramaService
                .encontrarFechaFinMasTardia(todasLasActividades);
        if (fechaFinMasTardia != null) {
            programaObra = programaObra.actualizarFechaFinDesdeActividades(fechaFinMasTardia);
        }

        // 9. Persistir programa actualizado
        programaObraRepository.save(programaObra);

        // 10. Retornar respuesta
        return new ActividadProgramadaResponse(actividad.getId().getValue(), actividad.getPartidaId(),
                actividad.getProgramaObraId(), actividad.getFechaInicio(), actividad.getFechaFin(),
                actividad.getDuracionDias(), actividad.getPredecesoras(), actividad.getVersion().intValue());
    }
}
