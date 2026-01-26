package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.cronograma.port.out.ActividadProgramadaRepository;
import com.budgetpro.domain.finanzas.cronograma.port.out.CronogramaSnapshotRepository;
import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.domain.finanzas.cronograma.service.CronogramaService;
import com.budgetpro.domain.finanzas.cronograma.service.SnapshotGeneratorService;
import com.budgetpro.shared.validation.JsonSchemaValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para crear los beans de CronogramaService y
 * SnapshotGeneratorService.
 */
@Configuration
public class CronogramaServiceConfig {

    @Bean
    public SnapshotGeneratorService snapshotGeneratorService(JsonSchemaValidator jsonSchemaValidator) {
        return new SnapshotGeneratorService(jsonSchemaValidator, jsonSchemaValidator);
    }

    @Bean
    public CronogramaService cronogramaService(ProgramaObraRepository programaObraRepository,
            ActividadProgramadaRepository actividadProgramadaRepository,
            CronogramaSnapshotRepository cronogramaSnapshotRepository,
            SnapshotGeneratorService snapshotGeneratorService, JsonSchemaValidator jsonSchemaValidator) {
        return new CronogramaService(programaObraRepository, actividadProgramadaRepository,
                cronogramaSnapshotRepository, snapshotGeneratorService, jsonSchemaValidator);
    }
}
