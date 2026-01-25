package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.ConfiguracionLaboralExtendidaResponse;
import com.budgetpro.application.rrhh.dto.HistorialFSRResponse;
import com.budgetpro.application.rrhh.port.in.ConsultarHistorialFSRUseCase;
import com.budgetpro.application.rrhh.port.out.ConfiguracionLaboralRepositoryPort;
import com.budgetpro.domain.rrhh.model.ConfiguracionLaboralExtendida;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConsultarHistorialFSRUseCaseImpl implements ConsultarHistorialFSRUseCase {

    private final ConfiguracionLaboralRepositoryPort repository;

    public ConsultarHistorialFSRUseCaseImpl(ConfiguracionLaboralRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public HistorialFSRResponse consultarHistorial(ProyectoId proyectoId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<ConfiguracionLaboralExtendida> configs;

        if (proyectoId != null) {
            configs = repository.findHistoryByProyecto(proyectoId, fechaInicio, fechaFin);
        } else {
            configs = repository.findHistoryGlobal(fechaInicio, fechaFin);
        }

        List<ConfiguracionLaboralExtendidaResponse> responses = configs.stream().map(this::mapToResponse)
                .collect(Collectors.toList());

        return new HistorialFSRResponse(responses);
    }

    private ConfiguracionLaboralExtendidaResponse mapToResponse(ConfiguracionLaboralExtendida config) {
        return new ConfiguracionLaboralExtendidaResponse(config.getId(), config.getProyectoId(),
                config.getFechaInicio(), config.getFechaFin(), config.esActiva(), config.calcularFSRBase(),
                config.calcularFSRExtendido(), config.getDiasAguinaldo(), config.getDiasVacaciones(),
                config.getPorcentajeSeguridadSocial(), config.getDiasNoTrabajados(), config.getDiasLaborablesAno(),
                config.getFactorHorasExtras(), config.getFactorTurnoNocturno(), config.getFactorRiesgo(),
                config.getFactorRegional());
    }
}
