package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.ConfiguracionLaboralExtendidaResponse;
import com.budgetpro.application.rrhh.dto.ConfigurarLaboralExtendidaCommand;
import com.budgetpro.application.rrhh.exception.ConfiguracionLaboralInvalidaException;
import com.budgetpro.application.rrhh.port.in.ConfigurarLaboralExtendidaUseCase;
import com.budgetpro.application.rrhh.port.out.ConfiguracionLaboralRepositoryPort;
import com.budgetpro.domain.rrhh.model.ConfiguracionLaboralExtendida;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class ConfigurarLaboralExtendidaUseCaseImpl implements ConfigurarLaboralExtendidaUseCase {

    private final ConfiguracionLaboralRepositoryPort repository;

    public ConfigurarLaboralExtendidaUseCaseImpl(ConfiguracionLaboralRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ConfiguracionLaboralExtendidaResponse configurar(ConfigurarLaboralExtendidaCommand command) {
        if (command.fechaInicio() == null) {
            throw new ConfiguracionLaboralInvalidaException("Fecha de inicio es obligatoria");
        }

        // 1. Find active configuration to close
        Optional<ConfiguracionLaboralExtendida> currentConfigOpt;
        if (command.proyectoId() != null) {
            currentConfigOpt = repository.findActiveByProyecto(command.proyectoId());
        } else {
            currentConfigOpt = repository.findGlobalActive();
        }

        // 2. Close existing if found
        if (currentConfigOpt.isPresent()) {
            ConfiguracionLaboralExtendida current = currentConfigOpt.get();
            if (!command.fechaInicio().isAfter(current.getFechaInicio())) {
                throw new ConfiguracionLaboralInvalidaException(
                        "Nueva configuración debe iniciar después de la actual");
            }
            // Close the day before the new one starts
            LocalDate fechaCierre = command.fechaInicio().minusDays(1);
            current.cerrar(fechaCierre);
            repository.save(current);
        }

        // 3. Create new configuration
        ConfiguracionLaboralExtendida newConfig = ConfiguracionLaboralExtendida.crear(command.proyectoId(),
                command.fechaInicio(), command.diasAguinaldo(), command.diasVacaciones(),
                command.porcentajeSeguridadSocial(), command.diasNoTrabajados(), command.diasLaborablesAno(),
                command.factorHorasExtras(), command.factorTurnoNocturno(), command.factorRiesgo(),
                command.factorRegional());

        // 4. Save new configuration
        ConfiguracionLaboralExtendida saved = repository.save(newConfig);

        // 5. Return response
        return mapToResponse(saved);
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
