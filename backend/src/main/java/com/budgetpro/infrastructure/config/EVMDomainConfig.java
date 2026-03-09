package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.evm.util.WorkingDayCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EVMDomainConfig {

    @Bean
    public WorkingDayCalculator workingDayCalculator() {
        return new WorkingDayCalculator();
    }
}
