package com.budgetpro.infrastructure.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro de correlación de requests para tracing distribuido.
 * 
 * Genera un Correlation ID único para cada request y lo almacena en el MDC (Mapped Diagnostic Context)
 * de SLF4J, permitiendo que todos los logs generados durante el procesamiento del request
 * incluyan automáticamente este identificador.
 * 
 * Características:
 * - Prioridad más alta: Se ejecuta primero en la cadena de filtros (@Order(Ordered.HIGHEST_PRECEDENCE))
 * - Reutiliza Correlation ID si viene en el header X-Correlation-ID
 * - Genera UUID si no viene en el header
 * - Limpia el MDC al finalizar el request (previene memory leaks)
 * - Agrega el Correlation ID al header de respuesta para que el cliente pueda rastrearlo
 * 
 * Uso:
 * El cliente puede enviar un header X-Correlation-ID para rastrear requests distribuidos.
 * Si no se envía, se genera uno automáticamente. Todos los logs del request incluirán
 * este ID en el patrón de logging configurado.
 * 
 * @see MDC
 * @see OncePerRequestFilter
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_CORRELATION_ID_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            // Obtener Correlation ID del header o generar uno nuevo
            String correlationId = extractOrGenerateCorrelationId(request);
            
            // Agregar Correlation ID al MDC para que esté disponible en todos los logs
            MDC.put(MDC_CORRELATION_ID_KEY, correlationId);
            
            // Agregar Correlation ID al header de respuesta para que el cliente lo vea
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            
            // Continuar con la cadena de filtros
            filterChain.doFilter(request, response);
            
        } finally {
            // Limpiar el MDC al finalizar el request para evitar memory leaks
            // Esto es crítico en aplicaciones con alta concurrencia
            MDC.clear();
        }
    }

    /**
     * Extrae el Correlation ID del header de la request, o genera uno nuevo si no existe.
     * 
     * @param request La request HTTP
     * @return El Correlation ID (del header o generado)
     */
    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        
        if (correlationId == null || correlationId.isBlank()) {
            // Generar nuevo UUID si no viene en el header
            correlationId = UUID.randomUUID().toString();
        }
        
        return correlationId;
    }
}
