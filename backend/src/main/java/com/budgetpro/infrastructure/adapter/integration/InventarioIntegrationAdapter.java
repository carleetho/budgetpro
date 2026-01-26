package com.budgetpro.infrastructure.adapter.integration;

import com.budgetpro.application.estimacion.dto.ConsumoMaterialResponse;
import com.budgetpro.application.estimacion.port.out.InventarioIntegrationPort;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

@Component
public class InventarioIntegrationAdapter implements InventarioIntegrationPort {

    private final RestTemplate restTemplate;
    // Assuming base URL is configured or hardcoded for internal service
    private final String inventoryServiceUrl = "http://localhost:8080/api/v1/inventarios"; // In-process call for now?
                                                                                           // Or mock?
    // Requirements say "calls existing Inventarios module".
    // If it's a modular monolith, we might inject a direct Service Bean instead of
    // HTTP.
    // However, instructions said "Adapters call existing Inventarios and RRHH APIs"
    // and usually implies an HTTP call or decoupled interface.
    // Given scope "Integration with Inventarios... for informational display" via
    // "Integration Ports".
    // I will assume HTTP or local service call. If it's a monolith, I should inject
    // the service directly if possible, but strict hexagonal says use port.
    // The requirement explicitly mentions "calls GET /api/v1/inventarios/consumo".
    // So I will implement HTTP call using RestTemplate.

    public InventarioIntegrationAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ConsumoMaterialResponse consultarConsumo(UUID proyectoId, UUID partidaId, LocalDate inicio, LocalDate fin) {
        try {
            String url = String.format("%s/consumo?proyectoId=%s&partidaId=%s&inicio=%s&fin=%s", inventoryServiceUrl,
                    proyectoId, partidaId, inicio, fin);

            return restTemplate.getForObject(url, ConsumoMaterialResponse.class);
        } catch (RestClientException e) {
            // "Handle cases where data is not available gracefully"
            // Return empty response on error
            return new ConsumoMaterialResponse(partidaId, java.math.BigDecimal.ZERO, "S/D", Collections.emptyList());
        }
    }
}
