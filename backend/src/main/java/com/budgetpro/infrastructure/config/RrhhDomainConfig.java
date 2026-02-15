package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.rrhh.service.CalculadorFSR;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RrhhDomainConfig {

    @Bean
    public CalculadorFSR calculadorFSR() {
        return new CalculadorFSR();
    }
}
