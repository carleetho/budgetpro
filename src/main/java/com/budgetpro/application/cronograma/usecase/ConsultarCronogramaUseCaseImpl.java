package com.budgetpro.application.cronograma.usecase;

import com.budgetpro.application.cronograma.dto.ActividadProgramadaResponse;
import com.budgetpro.application.cronograma.dto.CronogramaResponse;
import com.budgetpro.application.cronograma.port.in.ConsultarCronogramaUseCase;
import com.budgetpro.application.presupuesto.exception.ProyectoNoEncontradoException;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.port.out.ActividadProgramadaRepository;
import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.domain.finanzas.cronograma.service.CalculoCronogramaService;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para consultar el cronograma de un proyecto.
 */
@Service
public class ConsultarCronogramaUseCaseImpl implements ConsultarCronogramaUseCase {

    private final ProyectoRepository proyectoRepository;
    private final ProgramaObraRepository programaObraRepository;
    private final ActividadProgramadaRepository actividadProgramadaRepository;
    private final CalculoCronogramaService calculoCronogramaService;

    public ConsultarCronogramaUseCaseImpl(ProyectoRepository proyectoRepository,
                                         ProgramaObraRepository programaObraRepository,
                                         ActividadProgramadaRepository actividadProgramadaRepository,
                                         CalculoCronogramaService calculoCronogramaService) {
        this.proyectoRepository = proyectoRepository;
        this.programaObraRepository = programaObraRepository;
        this.actividadProgramadaRepository = actividadProgramadaRepository;
        this.calculoCronogramaService = calculoCronogramaService;
    }

    @Override
    @Transactional(readOnly = true)
    public CronogramaResponse consultar(UUID proyectoId) {
        // 1. Validar que el proyecto existe
        if (proyectoRepository.findById(ProyectoId.from(proyectoId)).isEmpty()) {
            throw new ProyectoNoEncontradoException(proyectoId);
        }

        // 2. Buscar el programa de obra
        ProgramaObra programaObra = programaObraRepository.findByProyectoId(proyectoId)
                .orElse(null);

        if (programaObra == null) {
            // Si no existe programa, retornar respuesta vacía
            return new CronogramaResponse(
                null,
                proyectoId,
                null,
                null,
                null,
                null,
                List.of(),
                null
            );
        }

        // 3. Buscar todas las actividades del programa
        List<com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada> actividades = 
                actividadProgramadaRepository.findByProgramaObraId(programaObra.getId().getValue());

        // 4. Recalcular duración total basándose en las actividades
        Integer duracionTotalDias = calculoCronogramaService.calcularDuracionTotal(programaObra, actividades);
        
        // 5. Calcular duración en meses (para cálculo de financiamiento)
        Integer duracionMeses = calculoCronogramaService.calcularDuracionMeses(duracionTotalDias);

        // 6. Mapear actividades a DTOs
        List<ActividadProgramadaResponse> actividadesResponse = actividades.stream()
                .map(actividad -> new ActividadProgramadaResponse(
                    actividad.getId().getValue(),
                    actividad.getPartidaId(),
                    actividad.getProgramaObraId(),
                    actividad.getFechaInicio(),
                    actividad.getFechaFin(),
                    actividad.getDuracionDias(),
                    actividad.getPredecesoras(),
                    actividad.getVersion().intValue()
                ))
                .collect(Collectors.toList());

        // 7. Retornar respuesta
        return new CronogramaResponse(
            programaObra.getId().getValue(),
            programaObra.getProyectoId(),
            programaObra.getFechaInicio(),
            programaObra.getFechaFinEstimada(),
            duracionTotalDias != null ? duracionTotalDias : programaObra.getDuracionTotalDias(),
            duracionMeses,
            actividadesResponse,
            programaObra.getVersion().intValue()
        );
    }
}
