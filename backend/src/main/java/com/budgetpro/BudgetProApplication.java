package com.budgetpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

// REGLA-102
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.budgetpro.infrastructure.persistence.repository")
@EnableScheduling
public class BudgetProApplication {

    // REGLA-100
    public static void main(String[] args) {
        SpringApplication.run(BudgetProApplication.class, args);
    }

}
