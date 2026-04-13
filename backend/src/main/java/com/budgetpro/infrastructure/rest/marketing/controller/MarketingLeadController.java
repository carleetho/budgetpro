package com.budgetpro.infrastructure.rest.marketing.controller;

import com.budgetpro.infrastructure.persistence.entity.marketing.LeadEntity;
import com.budgetpro.infrastructure.persistence.repository.marketing.LeadJpaRepository;
import com.budgetpro.infrastructure.rest.publico.dto.LeadResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/marketing/leads")
public class MarketingLeadController {

    private final LeadJpaRepository leadJpaRepository;

    public MarketingLeadController(LeadJpaRepository leadJpaRepository) {
        this.leadJpaRepository = leadJpaRepository;
    }

    @GetMapping
    public ResponseEntity<List<LeadResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size <= 0 || size > 200) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }
        List<LeadEntity> all = leadJpaRepository.findAll();
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        return ResponseEntity.ok(all.subList(from, to).stream().map(MarketingLeadController::toResponse).toList());
    }

    @GetMapping("/{leadId}")
    public ResponseEntity<LeadResponse> obtener(@PathVariable UUID leadId) {
        LeadEntity lead = leadJpaRepository.findById(leadId)
                .orElseThrow(() -> new EntityNotFoundException("Lead no encontrado."));
        return ResponseEntity.ok(toResponse(lead));
    }

    private static LeadResponse toResponse(LeadEntity lead) {
        return new LeadResponse(lead.getId(), lead.getEstado(), lead.getFechaSolicitud());
    }
}

