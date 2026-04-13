package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.rrhh.port.AsignacionSolapeValidator;
import com.budgetpro.domain.rrhh.service.CalculadorFSR;
import com.budgetpro.domain.rrhh.service.NoOpAsignacionSolapeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RrhhDomainConfig {

    @Bean
    public CalculadorFSR calculadorFSR() {
        return new CalculadorFSR();
    }

    /**
     * R-03 multi-sitio: sin política hasta decisión de PO; el puerto permanece cableable.
     */
    @Bean
    public AsignacionSolapeValidator asignacionSolapeValidator() {
        return new NoOpAsignacionSolapeValidator();
    }
}
