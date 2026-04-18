package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.rrhh.port.AsignacionSolapeValidator;
import com.budgetpro.domain.rrhh.service.CalculadorFSR;
import com.budgetpro.domain.rrhh.service.RegimenCivilSolapeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RrhhDomainConfig {

    @Bean
    public CalculadorFSR calculadorFSR() {
        return new CalculadorFSR();
    }

    /**
     * R-03: solape duro de intervalos de asignación (decisión PO / régimen civil).
     */
    @Bean
    public AsignacionSolapeValidator asignacionSolapeValidator() {
        return new RegimenCivilSolapeValidator();
    }
}
