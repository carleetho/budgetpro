package com.budgetpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.budgetpro.infrastructure.persistence.repository")
public class BudgetProApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudgetProApplication.class, args);
    }

}
