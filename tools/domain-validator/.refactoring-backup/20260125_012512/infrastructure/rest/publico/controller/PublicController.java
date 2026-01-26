package com.budgetpro.infrastructure.rest.publico.controller;

import com.budgetpro.application.marketing.service.LeadService;
import com.budgetpro.infrastructure.persistence.entity.marketing.LeadEntity;
import com.budgetpro.infrastructure.rest.publico.dto.CrearLeadRequest;
import com.budgetpro.infrastructure.rest.publico.dto.LeadResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Endpoints públicos (sin autenticación).
 */
@RestController
@RequestMapping("/api/public/v1")
public class PublicController {

    private final LeadService leadService;

    public PublicController(LeadService leadService) {
        this.leadService = leadService;
    }

    @PostMapping("/demo-request")
    public ResponseEntity<LeadResponse> crearDemoRequest(@Valid @RequestBody CrearLeadRequest request) {
        LeadEntity saved = leadService.crearLead(request);
        LeadResponse response = new LeadResponse(saved.getId(), saved.getEstado(), saved.getFechaSolicitud());
        return ResponseEntity
                .created(URI.create("/api/public/v1/demo-request/" + saved.getId()))
                .body(response);
    }
}
