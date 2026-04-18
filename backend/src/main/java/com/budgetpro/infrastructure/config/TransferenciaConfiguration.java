package com.budgetpro.infrastructure.config;

import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.logistica.transferencia.port.out.ExcepcionValidator;
import com.budgetpro.domain.logistica.transferencia.port.out.TransferenciaEventPublisher;
import com.budgetpro.domain.logistica.transferencia.service.TransferenciaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra {@link TransferenciaService} y los puertos mínimos que aún no tienen adaptador dedicado.
 * <p>
 * {@link ExcepcionValidator}: sin tabla de excepciones expuesta aún en persistencia; implementación
 * conservadora (no aprueba) hasta existir repositorio de dominio.
 */
@Configuration
public class TransferenciaConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaConfiguration.class);

    @Bean
    public ExcepcionValidator transferenciaExcepcionValidator() {
        return excepcionId -> false;
    }

    @Bean
    public TransferenciaEventPublisher transferenciaEventPublisher() {
        return event -> log.debug("Transferencia dominio (sin suscriptores): {}", event);
    }

    @Bean
    public TransferenciaService transferenciaService(InventarioRepository inventarioRepository,
            ExcepcionValidator transferenciaExcepcionValidator,
            TransferenciaEventPublisher transferenciaEventPublisher) {
        return new TransferenciaService(inventarioRepository, transferenciaExcepcionValidator,
                transferenciaEventPublisher);
    }
}
