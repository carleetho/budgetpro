package com.budgetpro.application.sobrecosto.usecase;

import com.budgetpro.application.sobrecosto.dto.ConfiguracionLaboralResponse;
import com.budgetpro.application.sobrecosto.dto.ConfigurarLaboralCommand;
import com.budgetpro.application.sobrecosto.port.in.ConfigurarLaboralUseCase;
import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral;
import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboralId;
import com.budgetpro.domain.finanzas.sobrecosto.port.out.ConfiguracionLaboralRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementación del caso de uso para configurar los parámetros laborales (FSR).
 */
@Service
public class ConfigurarLaboralUseCaseImpl implements ConfigurarLaboralUseCase {

    private final ConfiguracionLaboralRepository configuracionLaboralRepository;

    public ConfigurarLaboralUseCaseImpl(ConfiguracionLaboralRepository configuracionLaboralRepository) {
        this.configuracionLaboralRepository = configuracionLaboralRepository;
    }

    @Override
    @Transactional
    public ConfiguracionLaboralResponse configurar(ConfigurarLaboralCommand command) {
        ConfiguracionLaboral configuracion;

        if (command.proyectoId() == null) {
            // Configuración global (singleton)
            configuracion = configuracionLaboralRepository.findGlobal()
                    .orElseGet(() -> {
                        ConfiguracionLaboralId id = ConfiguracionLaboralId.nuevo();
                        return ConfiguracionLaboral.crearGlobal(
                                id,
                                command.diasAguinaldo() != null ? command.diasAguinaldo() : 0,
                                command.diasVacaciones() != null ? command.diasVacaciones() : 0,
                                command.porcentajeSeguridadSocial() != null ? command.porcentajeSeguridadSocial() : BigDecimal.ZERO,
                                command.diasNoTrabajados() != null ? command.diasNoTrabajados() : 0,
                                command.diasLaborablesAno() != null ? command.diasLaborablesAno() : 251
                        );
                    });
        } else {
            // Configuración por proyecto
            configuracion = configuracionLaboralRepository.findByProyectoId(command.proyectoId())
                    .orElseGet(() -> {
                        ConfiguracionLaboralId id = ConfiguracionLaboralId.nuevo();
                        return ConfiguracionLaboral.crearPorProyecto(
                                id,
                                command.proyectoId(),
                                command.diasAguinaldo() != null ? command.diasAguinaldo() : 0,
                                command.diasVacaciones() != null ? command.diasVacaciones() : 0,
                                command.porcentajeSeguridadSocial() != null ? command.porcentajeSeguridadSocial() : BigDecimal.ZERO,
                                command.diasNoTrabajados() != null ? command.diasNoTrabajados() : 0,
                                command.diasLaborablesAno() != null ? command.diasLaborablesAno() : 251
                        );
                    });
        }

        // Actualizar valores (si es necesario, crear métodos de actualización en el agregado)
        // Por ahora, recreamos la configuración con los nuevos valores
        if (command.proyectoId() == null) {
            ConfiguracionLaboralId id = configuracion.getId();
            configuracion = ConfiguracionLaboral.crearGlobal(
                    id,
                    command.diasAguinaldo() != null ? command.diasAguinaldo() : configuracion.getDiasAguinaldo(),
                    command.diasVacaciones() != null ? command.diasVacaciones() : configuracion.getDiasVacaciones(),
                    command.porcentajeSeguridadSocial() != null ? command.porcentajeSeguridadSocial() : configuracion.getPorcentajeSeguridadSocial(),
                    command.diasNoTrabajados() != null ? command.diasNoTrabajados() : configuracion.getDiasNoTrabajados(),
                    command.diasLaborablesAno() != null ? command.diasLaborablesAno() : configuracion.getDiasLaborablesAno()
            );
        } else {
            ConfiguracionLaboralId id = configuracion.getId();
            configuracion = ConfiguracionLaboral.crearPorProyecto(
                    id,
                    command.proyectoId(),
                    command.diasAguinaldo() != null ? command.diasAguinaldo() : configuracion.getDiasAguinaldo(),
                    command.diasVacaciones() != null ? command.diasVacaciones() : configuracion.getDiasVacaciones(),
                    command.porcentajeSeguridadSocial() != null ? command.porcentajeSeguridadSocial() : configuracion.getPorcentajeSeguridadSocial(),
                    command.diasNoTrabajados() != null ? command.diasNoTrabajados() : configuracion.getDiasNoTrabajados(),
                    command.diasLaborablesAno() != null ? command.diasLaborablesAno() : configuracion.getDiasLaborablesAno()
            );
        }

        // Persistir
        configuracionLaboralRepository.save(configuracion);

        // Retornar respuesta con FSR calculado
        return new ConfiguracionLaboralResponse(
            configuracion.getId().getValue(),
            configuracion.getProyectoId(),
            configuracion.getDiasAguinaldo(),
            configuracion.getDiasVacaciones(),
            configuracion.getPorcentajeSeguridadSocial(),
            configuracion.getDiasNoTrabajados(),
            configuracion.getDiasLaborablesAno(),
            configuracion.calcularFSR(),
            configuracion.getVersion().intValue()
        );
    }
}
