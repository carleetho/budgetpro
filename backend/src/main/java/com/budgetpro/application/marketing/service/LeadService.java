package com.budgetpro.application.marketing.service;

import com.budgetpro.infrastructure.notification.EmailNotificationService;
import com.budgetpro.infrastructure.persistence.entity.marketing.LeadEntity;
import com.budgetpro.infrastructure.persistence.entity.marketing.LeadEstado;
import com.budgetpro.infrastructure.persistence.repository.marketing.LeadJpaRepository;
import com.budgetpro.infrastructure.rest.publico.dto.CrearLeadRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servicio de negocio para Leads de marketing.
 */
@Service
public class LeadService {

    private final LeadJpaRepository leadJpaRepository;
    private final EmailNotificationService emailNotificationService;

    public LeadService(LeadJpaRepository leadJpaRepository,
                       EmailNotificationService emailNotificationService) {
        this.leadJpaRepository = leadJpaRepository;
        this.emailNotificationService = emailNotificationService;
    }

    public LeadEntity crearLead(CrearLeadRequest request) {
        LeadEntity lead = new LeadEntity();
        lead.setId(UUID.randomUUID());
        lead.setNombreContacto(request.nombreContacto());
        lead.setEmail(request.email());
        lead.setTelefono(request.telefono());
        lead.setNombreEmpresa(request.nombreEmpresa());
        lead.setRol(request.rol());
        lead.setEstado(LeadEstado.NUEVO);
        LeadEntity saved = leadJpaRepository.save(lead);

        emailNotificationService.enviarAlertaNuevoLead(saved);

        // TODO: Integración CRM - enviar lead a HubSpot API.

        return saved;
    }
}
