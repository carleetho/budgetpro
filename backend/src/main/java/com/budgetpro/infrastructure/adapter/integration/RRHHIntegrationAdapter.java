package com.budgetpro.infrastructure.adapter.integration;

import com.budgetpro.application.estimacion.dto.HorasLaborResponse;
import com.budgetpro.application.estimacion.port.out.RRHHIntegrationPort;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

@Component
public class RRHHIntegrationAdapter implements RRHHIntegrationPort {

    private final RestTemplate restTemplate;
    private final String rrhhServiceUrl = "http://localhost:8080/api/v1/rrhh";

    public RRHHIntegrationAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public HorasLaborResponse consultarHoras(UUID proyectoId, UUID partidaId, LocalDate inicio, LocalDate fin) {
        try {
            String url = String.format("%s/horas?proyectoId=%s&partidaId=%s&inicio=%s&fin=%s", rrhhServiceUrl,
                    proyectoId, partidaId, inicio, fin);

            return restTemplate.getForObject(url, HorasLaborResponse.class);
        } catch (RestClientException e) {
            // Graceful fallback
            return new HorasLaborResponse(partidaId, java.math.BigDecimal.ZERO, Collections.emptyList());
        }
    }
}
