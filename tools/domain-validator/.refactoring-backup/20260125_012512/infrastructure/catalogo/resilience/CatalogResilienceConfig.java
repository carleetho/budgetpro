package com.budgetpro.infrastructure.catalogo.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import com.budgetpro.domain.catalogo.exception.CatalogServiceException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuración de patrones de resiliencia para el catálogo.
 * 
 * Circuit Breaker: Se abre después de 5 fallos consecutivos
 * Retry: Hasta 3 intentos con backoff exponencial (1s, 2s, 4s)
 * Timeout: 5 segundos máximo
 * Rate Limiter: 100 requests/minuto por fuente de catálogo
 */
@Configuration
public class CatalogResilienceConfig {

    @Bean
    public CircuitBreakerConfig catalogCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% de fallos abre el circuito
                .waitDurationInOpenState(Duration.ofSeconds(30)) // Espera 30s antes de intentar de nuevo
                .slidingWindowSize(10) // Ventana deslizante de 10 llamadas
                .minimumNumberOfCalls(5) // Mínimo 5 llamadas antes de evaluar
                .permittedNumberOfCallsInHalfOpenState(3) // 3 llamadas en estado half-open
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(CatalogServiceException.class, RuntimeException.class)
                .build();
    }

    @Bean
    public RetryConfig catalogRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3) // Máximo 3 intentos
                .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                        Duration.ofSeconds(1), 2.0)) // Backoff exponencial (1s, 2s, 4s)
                .retryExceptions(CatalogServiceException.class, RuntimeException.class)
                .ignoreExceptions(com.budgetpro.domain.catalogo.exception.CatalogNotFoundException.class)
                .build();
    }

    @Bean
    public TimeLimiterConfig catalogTimeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5)) // Timeout de 5 segundos
                .build();
    }

    @Bean
    public RateLimiterConfig catalogRateLimiterConfig() {
        return RateLimiterConfig.custom()
                .limitForPeriod(100) // 100 requests por período
                .limitRefreshPeriod(Duration.ofMinutes(1)) // Período de 1 minuto
                .timeoutDuration(Duration.ofSeconds(1)) // Timeout de espera de 1s
                .build();
    }
}
