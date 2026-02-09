package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.finanzas.presupuesto.port.out.IntegrityAuditRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityAuditLog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para crear el bean de IntegrityAuditLog.
 * 
 * Este servicio de dominio debe ser agnóstico del framework (Hexagonal Architecture),
 * por lo que se configura manualmente aquí en la capa de infraestructura.
 */
@Configuration
public class IntegrityAuditLogConfig {

    @Bean
    public IntegrityAuditLog integrityAuditLog(IntegrityAuditRepository repository) {
        return new IntegrityAuditLog(repository);
    }
}
