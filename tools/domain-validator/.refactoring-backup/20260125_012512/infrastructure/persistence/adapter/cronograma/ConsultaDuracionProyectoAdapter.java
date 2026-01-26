package com.budgetpro.infrastructure.persistence.adapter.cronograma;

import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.port.out.ActividadProgramadaRepository;
import com.budgetpro.domain.finanzas.cronograma.port.out.ConsultaDuracionProyectoPort;
import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.domain.finanzas.cronograma.service.CalculoCronogramaService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador que implementa ConsultaDuracionProyectoPort.
 * 
 * Este adaptador es usado por el Motor de Costos (Mov 9) para obtener el Tiempo de Construcción (TC)
 * necesario para calcular el Financiamiento y los Indirectos de Campo.
 */
@Component
public class ConsultaDuracionProyectoAdapter implements ConsultaDuracionProyectoPort {

    private final ProgramaObraRepository programaObraRepository;
    private final ActividadProgramadaRepository actividadProgramadaRepository;
    private final CalculoCronogramaService calculoCronogramaService;

    public ConsultaDuracionProyectoAdapter(ProgramaObraRepository programaObraRepository,
                                           ActividadProgramadaRepository actividadProgramadaRepository,
                                           CalculoCronogramaService calculoCronogramaService) {
        this.programaObraRepository = programaObraRepository;
        this.actividadProgramadaRepository = actividadProgramadaRepository;
        this.calculoCronogramaService = calculoCronogramaService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> getDuracionMeses(UUID proyectoId) {
        // 1. Buscar el programa de obra del proyecto
        Optional<ProgramaObra> programaObraOpt = programaObraRepository.findByProyectoId(proyectoId);
        
        if (programaObraOpt.isEmpty()) {
            return Optional.empty();
        }

        ProgramaObra programaObra = programaObraOpt.get();

        // 2. Buscar todas las actividades del programa
        java.util.List<com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada> actividades =
                actividadProgramadaRepository.findByProgramaObraId(programaObra.getId().getValue());

        // 3. Calcular duración en meses usando el servicio de dominio
        Integer duracionMeses = calculoCronogramaService.calcularDuracionMeses(programaObra, actividades);

        return Optional.ofNullable(duracionMeses);
    }
}
